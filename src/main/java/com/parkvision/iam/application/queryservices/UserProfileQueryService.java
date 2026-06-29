package com.parkvision.iam.application.queryservices;

import com.parkvision.iam.domain.model.entities.UserProfile;
import com.parkvision.iam.domain.queries.GetUserProfileQuery;
import com.parkvision.iam.infrastructure.persistence.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileQueryService {

    private final UserProfileRepository userProfileRepository;

    public Optional<UserProfile> handle(GetUserProfileQuery query) {
        return userProfileRepository.findById(query.userId());
    }
}
