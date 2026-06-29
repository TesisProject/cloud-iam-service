package com.parkvision.iam.infrastructure.persistence;

import com.parkvision.iam.domain.model.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {}
