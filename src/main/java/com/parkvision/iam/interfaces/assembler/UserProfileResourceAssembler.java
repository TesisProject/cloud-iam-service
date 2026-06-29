package com.parkvision.iam.interfaces.assembler;

import com.parkvision.iam.domain.model.entities.UserProfile;
import com.parkvision.iam.interfaces.resource.UserProfileResource;
import org.springframework.stereotype.Component;

@Component
public class UserProfileResourceAssembler {

    public UserProfileResource toResource(UserProfile profile) {
        return new UserProfileResource(
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.isActive(),
                profile.getAvatarUrl(),
                profile.getBio(),
                profile.getUpdatedAt()
        );
    }
}
