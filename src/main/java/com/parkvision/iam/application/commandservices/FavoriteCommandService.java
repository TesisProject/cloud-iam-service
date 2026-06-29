package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.AddFavoriteCommand;
import com.parkvision.iam.domain.commands.RemoveFavoriteCommand;
import com.parkvision.iam.domain.exceptions.FavoriteAlreadyExistsException;
import com.parkvision.iam.domain.exceptions.FavoriteNotFoundException;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.domain.model.entities.Favorite;
import com.parkvision.iam.domain.model.entities.FavoriteId;
import com.parkvision.iam.infrastructure.persistence.FavoriteRepository;
import com.parkvision.iam.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteCommandService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    @Transactional
    public void handle(AddFavoriteCommand command) {
        FavoriteId id = new FavoriteId(command.userId(), command.zoneId());
        if (favoriteRepository.existsById(id)) {
            throw new FavoriteAlreadyExistsException(command.userId(), command.zoneId());
        }
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        favoriteRepository.save(new Favorite(user, command.zoneId()));
    }

    @Transactional
    public void handle(RemoveFavoriteCommand command) {
        FavoriteId id = new FavoriteId(command.userId(), command.zoneId());
        if (!favoriteRepository.existsById(id)) {
            throw new FavoriteNotFoundException(command.userId(), command.zoneId());
        }
        favoriteRepository.deleteById(id);
    }
}
