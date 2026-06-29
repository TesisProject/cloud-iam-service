package com.parkvision.iam.interfaces.assembler;

import com.parkvision.iam.domain.model.entities.Favorite;
import com.parkvision.iam.interfaces.resource.FavoriteResource;
import org.springframework.stereotype.Component;

@Component
public class FavoriteResourceAssembler {

    public FavoriteResource toResource(Favorite favorite) {
        return new FavoriteResource(
                favorite.getId().getUserId(),
                favorite.getId().getZoneId(),
                favorite.getSavedAt()
        );
    }
}
