package com.example.demo.auth;

import com.example.demo.Security.JwtService;
import com.example.demo.email.EmailService;
import com.example.demo.email.EmailTemplateName;
import com.example.demo.role.RoleRepository;
import com.example.demo.user.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRespository userRespository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Value("$application.mailing.frontend.activation-url}")
    private String activationUrl;
    private String baseUrl = "http://localhost:8088/";
    private int tokenExpirationMinutes = 30;



    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER").orElseThrow(() -> new IllegalStateException("ROLE USER was not initialized"));
        var user = User.builder()
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRespository.save(user);
        sendValidationEmail(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = ((User)auth.getPrincipal());
        return AuthenticationResponse.builder().token(generateToken(user)).build();
    }

    public String forgetPassword(String email) throws MessagingException {
        var user = userRespository.findByEmail(email).orElseThrow(() ->new IllegalStateException("ROLE USER was not initialized"));
        sendPasswordResetEmail(user,jwtService.generateForgetPasswordToken(user, TokenType.PASSWORD_RESET));
        return "Password reset email sent";
    }



    public void resetPassword(ResetPasswordRequest request) {
        var user = userRespository.findByEmail(request.getEmail()).orElseThrow(() ->new IllegalStateException("ROLE USER was not initialized"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRespository.save(user);
    }

    private Date calculateExpiryDate() {
        long expiryTimeInMillis = 1000 * 60 * 60; // 1 hour
        return   new Date(System.currentTimeMillis() + expiryTimeInMillis);
    }

    private void sendPasswordResetEmail(User user,String token) throws MessagingException {
        var newToken = generateToken(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(user.getEmail(),user.getFullUsername(), EmailTemplateName.ACTIVATE_ACCOUNT,generatePasswordResetUrl(newToken),null,"Account activation");

    }

    private String generatePasswordResetUrl(String token) {
        return baseUrl + "api/v1/auth/reset-password?token=" + token;
    }

    private String generateAndSaveActivationToken(User user) {
        //generate token
        String generatedToken = generateActivationCode(6);
        var token = Token.builder().token(generatedToken).createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusMinutes(15)).user(user).build();
        tokenRepository.save(token);
        return generatedToken;
    }

    private String generateToken(User user){
        var claims = new HashMap<String, Object>();
        claims.put("fullName",user.getFullUsername());
        return jwtService.generateToken(claims,user, TokenType.AUTHENTICATION.toString());
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder activationCodeBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            activationCodeBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return activationCodeBuilder.toString();
    }

    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())){
            sendValidationEmail(savedToken.getUser());
            throw new IllegalStateException("Token expired");
        }
        var user = userRespository.findById(savedToken.getUser().getId()).orElseThrow(() -> new IllegalStateException("User not found"));
        user.setEnabled(true);
        userRespository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);

    }
}
