package com.parkvision.iam.domain.model.aggregates;

import com.parkvision.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "role")
@Getter
public class Role extends AuditableAbstractAggregateRoot<Role> {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private boolean isActive = true;

    protected Role() {}

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
    }

    public void updateInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
