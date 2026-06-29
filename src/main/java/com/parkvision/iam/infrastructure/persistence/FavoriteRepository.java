package com.parkvision.iam.infrastructure.persistence;

import com.parkvision.iam.domain.model.entities.Favorite;
import com.parkvision.iam.domain.model.entities.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findById_UserId(Long userId);
}
