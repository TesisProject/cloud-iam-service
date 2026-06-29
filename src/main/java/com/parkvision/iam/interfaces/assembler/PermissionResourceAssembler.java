package com.parkvision.iam.interfaces.assembler;

import com.parkvision.iam.domain.model.entities.Permission;
import com.parkvision.iam.interfaces.resource.PermissionResource;
import org.springframework.stereotype.Component;

@Component
public class PermissionResourceAssembler {

    public PermissionResource toResource(Permission permission) {
        return new PermissionResource(
                permission.getId(),
                permission.getName(),
                permission.getResource(),
                permission.getAction(),
                permission.getDescription()
        );
    }
}
