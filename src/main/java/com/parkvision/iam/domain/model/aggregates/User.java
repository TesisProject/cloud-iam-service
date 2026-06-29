package com.parkvision.iam.domain.model.aggregates;

import com.parkvision.iam.domain.commands.SignUpCommand;
import com.parkvision.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "iam_user")
@Getter
public class User extends AuditableAbstractAggregateRoot<User> {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    protected User() {}

    public User(SignUpCommand command, String hashedPassword, Role role) {
        this.email = command.email();
        this.passwordHash = hashedPassword;
        this.role = role;
        this.isActive = true;
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String hashedPassword) {
        this.passwordHash = hashedPassword;
    }

    public void setOtp(String otp) {
        this.otpCode = otp;
        this.otpExpiresAt = LocalDateTime.now().plusMinutes(10);
    }

    public boolean isOtpValid(String otp) {
        return otpCode != null && otpCode.equals(otp)
                && otpExpiresAt != null && LocalDateTime.now().isBefore(otpExpiresAt);
    }

    public void clearOtp() {
        this.otpCode = null;
        this.otpExpiresAt = null;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
