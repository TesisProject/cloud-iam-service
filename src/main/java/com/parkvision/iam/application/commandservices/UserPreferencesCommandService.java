package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.UpdateUserPreferencesCommand;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.domain.model.entities.UserPreferences;
import com.parkvision.iam.infrastructure.persistence.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferencesCommandService {

    private final UserPreferencesRepository userPreferencesRepository;

    @Transactional
    public UserPreferences handle(UpdateUserPreferencesCommand command) {
        UserPreferences preferences = userPreferencesRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        preferences.updateInfo(command);
        return userPreferencesRepository.save(preferences);
    }
}
