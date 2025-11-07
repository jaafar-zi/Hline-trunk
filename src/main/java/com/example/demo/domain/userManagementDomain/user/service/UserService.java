package com.example.demo.domain.userManagementDomain.user.service;

import com.example.demo.domain.userManagementDomain.role.entity.Role;
import com.example.demo.domain.userManagementDomain.user.dto.UserDTO;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.example.demo.domain.userManagementDomain.user.entity.UserGroup;
import com.example.demo.domain.userManagementDomain.user.exception.*;
import com.example.demo.userManagementDomain.user.exception.*;
import com.example.demo.domain.userManagementDomain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

                            /*** Guard Methods ***/

    private void assertExists(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private User fetchAndCheck(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (user.isAccountLocked()) {
            throw new UserAccountLockedException(userId);
        }
        if (!user.isEnabled()) {
            throw new UserDisabledException(userId);
        }
        return user;
    }

    private User updateSimple(Integer userId, Consumer<User> updater) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        updater.accept(user);
        return userRepository.save(user);
    }

    private User updateAndSave(Integer userId, Consumer<User> updater) {
        User user = fetchAndCheck(userId);
        updater.accept(user);
        return userRepository.save(user);
    }

    /***             Public API Methods           ***/

    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toDto);
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByFirstname(username)) // fallback if needed
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
    }

    public Optional<User> findByFirstName(String username) {
        return userRepository.findByFirstname(username);
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public UserDTO findByPrincipal(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found for principal"));
        return toDto(user);
    }

    public void disableUser(Integer userId) {
        assertExists(userId);
        updateSimple(userId, u -> u.setEnabled(false));
    }

    public void enableUser(Integer userId) {
        assertExists(userId);
        updateSimple(userId, u -> u.setEnabled(true));
    }

    public void lockUser(Integer userId) {
        assertExists(userId);
        updateSimple(userId, u -> u.setAccountLocked(true));
    }

    public void unlockUser(Integer userId) {
        assertExists(userId);
        updateSimple(userId, u -> u.setAccountLocked(false));
    }

    public Set<Role> getUserRoles(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getRoles();
    }

    public UserDTO update(Integer userId, UserDTO dto) {
        User user = fetchAndCheck(userId);
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setEnabled(dto.isEnabled());
        user.setAccountLocked(dto.isAccountLocked());
        return toDto(userRepository.save(user));
    }

    public void updateEmail(Integer userId, String newEmail) {
        User user = fetchAndCheck(userId);
        if (!EMAIL_REGEX.matcher(newEmail).matches()) {
            throw new InvalidEmailException(newEmail);
        }
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new UsernameAlreadyExistsException(newEmail);
        }
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    public void updatePassword(Integer userId, String rawPassword) {
        User user = fetchAndCheck(userId);
        // (Optional) enforce password rules here, else:
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    public void updateProfile(Integer userId,
                              String firstname,
                              String lastname,
                              LocalDate dateOfBirth) {
        User user = fetchAndCheck(userId);
        if (firstname == null || firstname.isBlank()
                || lastname == null  || lastname.isBlank()
                || dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new InvalidUserProfileException(
                    "Firstname/lastname must not be blank and DOB must be in the past");
        }
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setDateOfBirth(dateOfBirth);
        userRepository.save(user);
    }

    public void assignRole(Integer userId, Role role) {
        updateAndSave(userId, u -> u.getRoles().add(role));
    }

    public void removeRole(Integer userId, Role role) {
        updateAndSave(userId, u -> {
            if (!u.getRoles().contains(role)) {
//                throw new RoleNotAssignedException(role.getName());
            }
            u.getRoles().remove(role);
        });
    }

    public void assignUserGroup(Integer userId, UserGroup group) {
        updateAndSave(userId, u -> u.setUserGroup(group));
    }

    public void removeUserGroup(Integer userId) {
        updateAndSave(userId, u -> u.setUserGroup(null));
    }

                            /*** Mapper ***/

    public UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .enabled(user.isEnabled())
                .accountLocked(user.isAccountLocked())
                .userGroupId(
                        Optional.ofNullable(user.getUserGroup())
                                .map(UserGroup::getId)
                                .map(Math::toIntExact)
                                .orElse(null))
                .build();
    }
}
