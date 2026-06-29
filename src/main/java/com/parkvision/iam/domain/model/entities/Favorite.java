package com.parkvision.iam.domain.model.entities;

import com.parkvision.iam.domain.model.aggregates.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorite")
@Getter
public class Favorite {

    @EmbeddedId
    private FavoriteId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    protected Favorite() {}

    public Favorite(User user, Long zoneId) {
        this.id = new FavoriteId(user.getId(), zoneId);
        this.user = user;
        this.savedAt = LocalDateTime.now();
    }
}
