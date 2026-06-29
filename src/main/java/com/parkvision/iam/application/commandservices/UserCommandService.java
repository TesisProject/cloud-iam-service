package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.ChangePasswordCommand;
import com.parkvision.iam.domain.commands.ChangeUserRoleCommand;
import com.parkvision.iam.domain.commands.SignUpCommand;
import com.parkvision.iam.domain.commands.ToggleUserStatusCommand;
import com.parkvision.iam.domain.commands.UpdateUserCommand;
import com.parkvision.iam.domain.exceptions.InvalidPasswordException;
import com.parkvision.iam.domain.exceptions.UserAlreadyExistsException;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.domain.model.aggregates.Role;
import com.parkvision.iam.domain.model.aggregates.User;
import java.util.Optional;
import com.parkvision.iam.domain.model.entities.UserPreferences;
import com.parkvision.iam.domain.model.entities.UserProfile;
import com.parkvision.iam.infrastructure.persistence.RoleRepository;
import com.parkvision.iam.infrastructure.persistence.UserPreferencesRepository;
import com.parkvision.iam.infrastructure.persistence.UserProfileRepository;
import com.parkvision.iam.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("email", command.email());
        }
        String roleName = command.roleName() != null ? command.roleName() : "USER";
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.findByName("USER")
                        .orElseThrow(() -> new IllegalStateException("Default role USER not found")));

        String hashedPassword = passwordEncoder.encode(command.password());
        User user = new User(command, hashedPassword, role);
        userRepository.save(user);

        userProfileRepository.save(new UserProfile(user));
        userPreferencesRepository.save(new UserPreferences(user));

        return user.getId();
    }

    @Transactional
    public Optional<User> handle(UpdateUserCommand command) {
        return userRepository.findById(command.userId()).map(user -> {
            if (command.email() != null && !command.email().equals(user.getEmail())) {
                if (userRepository.existsByEmail(command.email())) {
                    throw new UserAlreadyExistsException("email", command.email());
                }
                user.updateEmail(command.email());
            }
            if (command.newPassword() != null && !command.newPassword().isBlank()) {
                user.updatePassword(passwordEncoder.encode(command.newPassword()));
            }
            return userRepository.save(user);
        });
    }

    @Transactional
    public Optional<User> handle(ChangeUserRoleCommand command) {
        return userRepository.findById(command.userId()).map(user -> {
            Role role = roleRepository.findByName(command.roleName())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + command.roleName()));
            user.changeRole(role);
            return userRepository.save(user);
        });
    }

    @Transactional
    public Optional<User> handle(ToggleUserStatusCommand command) {
        return userRepository.findById(command.userId()).map(user -> {
            user.setActive(command.active());
            return userRepository.save(user);
        });
    }

    @Transactional
    public void handle(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }
        user.updatePassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
    }
}
