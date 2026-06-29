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
@Table(name = "user_profile")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "text")
    private String bio;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected UserProfile() {}

    public UserProfile(User user) {
        this.user = user;
        this.isActive = true;
    }

    public void updateInfo(com.parkvision.iam.domain.commands.UpdateUserProfileCommand command) {
        this.firstName = command.firstName();
        this.lastName = command.lastName();
        this.phone = command.phone();
        this.avatarUrl = command.avatarUrl();
        this.bio = command.bio();
    }
}
