package com.parkvision.iam.domain.model.entities;

import com.parkvision.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "permission")
@Getter
public class Permission extends AuditableModel {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String resource;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 300)
    private String description;

    protected Permission() {}

    public Permission(String name, String resource, String action, String description) {
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
    }

    public void updateInfo(String name, String resource, String action, String description) {
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
    }
}
