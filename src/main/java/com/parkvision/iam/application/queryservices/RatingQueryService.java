package com.parkvision.iam.application.queryservices;

import com.parkvision.iam.domain.model.entities.Rating;
import com.parkvision.iam.domain.queries.GetRatingsByUserQuery;
import com.parkvision.iam.domain.queries.GetRatingsByZoneQuery;
import com.parkvision.iam.infrastructure.persistence.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingQueryService {

    private final RatingRepository ratingRepository;

    public List<Rating> handle(GetRatingsByUserQuery query) {
        return ratingRepository.findById_UserId(query.userId());
    }

    public List<Rating> handle(GetRatingsByZoneQuery query) {
        return ratingRepository.findById_ZoneId(query.zoneId());
    }
}
