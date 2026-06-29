package com.parkvision.iam.application.queryservices;

import com.parkvision.iam.domain.model.entities.UserPreferences;
import com.parkvision.iam.domain.queries.GetUserPreferencesQuery;
import com.parkvision.iam.infrastructure.persistence.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPreferencesQueryService {

    private final UserPreferencesRepository userPreferencesRepository;

    public Optional<UserPreferences> handle(GetUserPreferencesQuery query) {
        return userPreferencesRepository.findById(query.userId());
    }
}
