package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.UpdateUserProfileCommand;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.domain.model.entities.UserProfile;
import com.parkvision.iam.infrastructure.persistence.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileCommandService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfile handle(UpdateUserProfileCommand command) {
        UserProfile profile = userProfileRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        profile.updateInfo(command);
        return userProfileRepository.save(profile);
    }
}
