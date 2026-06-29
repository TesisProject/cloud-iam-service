package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.ForgotPasswordCommand;
import com.parkvision.iam.domain.commands.ResetPasswordCommand;
import com.parkvision.iam.domain.commands.VerifyOtpCommand;
import com.parkvision.iam.domain.exceptions.InvalidOtpException;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.infrastructure.email.EmailService;
import com.parkvision.iam.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PasswordResetCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void handle(ForgotPasswordCommand command) {
        var user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException(command.email()));
        String otp = generateOtp();
        user.setOtp(otp);
        userRepository.save(user);
        emailService.sendOtp(user.getEmail(), otp);
    }

    @Transactional(readOnly = true)
    public void handle(VerifyOtpCommand command) {
        var user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException(command.email()));
        if (!user.isOtpValid(command.otp())) {
            throw new InvalidOtpException();
        }
    }

    @Transactional
    public void handle(ResetPasswordCommand command) {
        var user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException(command.email()));
        if (!user.isOtpValid(command.otp())) {
            throw new InvalidOtpException();
        }
        user.updatePassword(passwordEncoder.encode(command.newPassword()));
        user.clearOtp();
        userRepository.save(user);
    }

    private String generateOtp() {
        return String.format("%05d", new SecureRandom().nextInt(100_000));
    }
}
