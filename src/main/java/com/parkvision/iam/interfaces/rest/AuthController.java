package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.AuthenticationCommandService;
import com.parkvision.iam.application.commandservices.PasswordResetCommandService;
import com.parkvision.iam.domain.commands.ForgotPasswordCommand;
import com.parkvision.iam.domain.commands.ResetPasswordCommand;
import com.parkvision.iam.domain.commands.SignInCommand;
import com.parkvision.iam.domain.commands.VerifyOtpCommand;
import com.parkvision.iam.interfaces.resource.AuthenticationResource;
import com.parkvision.iam.interfaces.resource.ForgotPasswordRequest;
import com.parkvision.iam.interfaces.resource.MessageResource;
import com.parkvision.iam.interfaces.resource.ResetPasswordRequest;
import com.parkvision.iam.interfaces.resource.SignInRequest;
import com.parkvision.iam.interfaces.resource.VerifyOtpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/iam/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Autenticación y emisión de tokens JWT")
public class AuthController {

    private final AuthenticationCommandService authenticationCommandService;
    private final PasswordResetCommandService passwordResetCommandService;

    @PostMapping("/sign-in")
    @Operation(summary = "Iniciar sesión y obtener un token JWT")
    public ResponseEntity<AuthenticationResource> signIn(@Valid @RequestBody SignInRequest request) {
        var command = new SignInCommand(request.email(), request.password());
        var result = authenticationCommandService.handle(command);
        return ResponseEntity.ok(new AuthenticationResource(
                result.token(),
                "Bearer",
                result.user().getId(),
                result.user().getEmail(),
                result.user().getRole().getName()
        ));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar código OTP para recuperación de contraseña")
    public ResponseEntity<MessageResource> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetCommandService.handle(new ForgotPasswordCommand(request.email()));
        return ResponseEntity.ok(new MessageResource("OTP sent to " + request.email()));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verificar que el OTP sea correcto y no haya expirado")
    public ResponseEntity<MessageResource> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        passwordResetCommandService.handle(new VerifyOtpCommand(request.email(), request.otp()));
        return ResponseEntity.ok(new MessageResource("OTP is valid"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña usando el OTP verificado")
    public ResponseEntity<MessageResource> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetCommandService.handle(new ResetPasswordCommand(request.email(), request.otp(), request.newPassword()));
        return ResponseEntity.ok(new MessageResource("Password updated successfully"));
    }
}
