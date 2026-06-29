package com.parkvision.iam.infrastructure.persistence;

import com.parkvision.iam.domain.model.entities.Rating;
import com.parkvision.iam.domain.model.entities.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, RatingId> {
    List<Rating> findById_UserId(Long userId);
    List<Rating> findById_ZoneId(Long zoneId);
}
