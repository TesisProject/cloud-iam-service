package com.parkvision.iam.domain.model.entities;

import com.parkvision.iam.domain.model.aggregates.Role;
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
@Table(name = "role_permission")
@Getter
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    protected RolePermission() {}

    public RolePermission(Role role, Permission permission) {
        this.id = new RolePermissionId(role.getId(), permission.getId());
        this.role = role;
        this.permission = permission;
        this.grantedAt = LocalDateTime.now();
    }
}
