package com.parkvision.iam.interfaces.assembler;

import com.parkvision.iam.domain.model.entities.Rating;
import com.parkvision.iam.interfaces.resource.RatingResource;
import org.springframework.stereotype.Component;

@Component
public class RatingResourceAssembler {

    public RatingResource toResource(Rating rating) {
        return new RatingResource(
                rating.getId().getUserId(),
                rating.getId().getZoneId(),
                rating.getStars(),
                rating.getComment(),
                rating.getType(),
                rating.isReviewed(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}
