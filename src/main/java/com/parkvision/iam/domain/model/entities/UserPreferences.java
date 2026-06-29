package com.parkvision.iam.domain.model.entities;

import com.parkvision.iam.domain.model.aggregates.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class UserPreferences {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "dark_mode", nullable = false)
    private boolean darkMode = false;

    @Column(nullable = false, length = 10)
    private String language = "es";

    @Column(name = "alert_free_space", nullable = false)
    private boolean alertFreeSpace = true;

    @Column(name = "alert_saturated", nullable = false)
    private boolean alertSaturated = true;

    @Column(name = "alert_camera_failure", nullable = false)
    private boolean alertCameraFailure = true;

    @Column(name = "alert_radius_m", nullable = false)
    private int alertRadiusM = 500;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected UserPreferences() {}

    public UserPreferences(User user) {
        this.user = user;
        this.darkMode = false;
        this.language = "es";
        this.alertFreeSpace = true;
        this.alertSaturated = true;
        this.alertCameraFailure = true;
        this.alertRadiusM = 500;
    }

    public void updateInfo(com.parkvision.iam.domain.commands.UpdateUserPreferencesCommand command) {
        this.darkMode = command.darkMode();
        this.language = command.language();
        this.alertFreeSpace = command.alertFreeSpace();
        this.alertSaturated = command.alertSaturated();
        this.alertCameraFailure = command.alertCameraFailure();
        this.alertRadiusM = command.alertRadiusM();
    }
}
