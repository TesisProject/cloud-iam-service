package com.parkvision.iam.interfaces.assembler;

import com.parkvision.iam.domain.model.aggregates.User;
import com.parkvision.iam.interfaces.resource.UserResource;
import org.springframework.stereotype.Component;

@Component
public class UserResourceAssembler {

    public UserResource toResource(User user) {
        return new UserResource(
                user.getId(),
                user.getEmail(),
                user.getRole().getName(),
                user.isActive(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
