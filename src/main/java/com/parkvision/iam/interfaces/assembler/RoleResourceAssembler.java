package com.parkvision.iam.interfaces.assembler;

import com.parkvision.iam.domain.model.aggregates.Role;
import com.parkvision.iam.interfaces.resource.RoleResource;
import org.springframework.stereotype.Component;

@Component
public class RoleResourceAssembler {

    public RoleResource toResource(Role role) {
        return new RoleResource(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
