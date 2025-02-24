package com.example.demo.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(String to,String username,EmailTemplateName emailTemplate,String confirmationUrl,String activationCode, String subject) throws MessagingException {
        String templateName;
        if (emailTemplate == null){
            templateName = "confirm-email";
        }
        else {
            templateName  = emailTemplate.name();
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name());
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        System.out.println("reset password url logging" + confirmationUrl);
        properties.put("confirmationUrl", confirmationUrl);
//        properties.put("activationCode", StringUtils.isEmpty(activationCode) ? "" : activationCode);
        properties.put("activationCode",activationCode);

        Context context = new Context();
        context.setVariables(properties);

        mimeMessageHelper.setFrom("noreply@example.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);

        String template = templateEngine.process(templateName, context);

        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);
    }

}
