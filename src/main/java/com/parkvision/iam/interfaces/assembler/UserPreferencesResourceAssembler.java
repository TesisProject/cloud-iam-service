package com.parkvision.iam.interfaces.assembler;

import com.parkvision.iam.domain.model.entities.UserPreferences;
import com.parkvision.iam.interfaces.resource.UserPreferencesResource;
import org.springframework.stereotype.Component;

@Component
public class UserPreferencesResourceAssembler {

    public UserPreferencesResource toResource(UserPreferences preferences) {
        return new UserPreferencesResource(
                preferences.getUserId(),
                preferences.isDarkMode(),
                preferences.getLanguage(),
                preferences.isAlertFreeSpace(),
                preferences.isAlertSaturated(),
                preferences.isAlertCameraFailure(),
                preferences.getAlertRadiusM(),
                preferences.getUpdatedAt()
        );
    }
}
