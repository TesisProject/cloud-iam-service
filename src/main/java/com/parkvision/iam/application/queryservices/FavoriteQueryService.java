package com.parkvision.iam.application.queryservices;

import com.parkvision.iam.domain.model.entities.Favorite;
import com.parkvision.iam.domain.queries.GetFavoritesByUserQuery;
import com.parkvision.iam.infrastructure.persistence.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteQueryService {

    private final FavoriteRepository favoriteRepository;

    public List<Favorite> handle(GetFavoritesByUserQuery query) {
        return favoriteRepository.findById_UserId(query.userId());
    }
}
