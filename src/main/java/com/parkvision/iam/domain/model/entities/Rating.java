package com.parkvision.iam.domain.model.entities;

import com.parkvision.iam.domain.commands.UpdateRatingCommand;
import com.parkvision.iam.domain.model.aggregates.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "rating")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Rating {

    @EmbeddedId
    private RatingId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Short stars;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(length = 30)
    private String type;

    @Column(name = "is_reviewed", nullable = false)
    private boolean isReviewed = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Rating() {}

    public Rating(User user, Long zoneId, Short stars, String comment, String type) {
        this.id = new RatingId(user.getId(), zoneId);
        this.user = user;
        this.stars = stars;
        this.comment = comment;
        this.type = type;
        this.isReviewed = false;
    }

    public void update(UpdateRatingCommand command) {
        this.stars = command.stars();
        this.comment = command.comment();
        this.type = command.type();
    }
}
