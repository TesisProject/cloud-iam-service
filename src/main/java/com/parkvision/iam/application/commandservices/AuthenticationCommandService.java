package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.SignInCommand;
import com.parkvision.iam.domain.exceptions.InvalidCredentialsException;
import com.parkvision.iam.infrastructure.persistence.UserRepository;
import com.parkvision.iam.infrastructure.tokens.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResult handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email())
                .filter(u -> u.isActive() && passwordEncoder.matches(command.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        user.recordLogin();
        userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user);
        return new AuthResult(user, token);
    }
}
