package com.parkvision.iam.infrastructure.persistence;

import com.parkvision.iam.domain.model.entities.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {}
