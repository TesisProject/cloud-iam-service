package com.parkvision.iam.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FavoriteId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "zone_id")
    private Long zoneId;

    protected FavoriteId() {}

    public FavoriteId(Long userId, Long zoneId) {
        this.userId = userId;
        this.zoneId = zoneId;
    }

    public Long getUserId() { return userId; }
    public Long getZoneId() { return zoneId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(zoneId, that.zoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, zoneId);
    }
}
