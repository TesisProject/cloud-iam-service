package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.CreateRatingCommand;
import com.parkvision.iam.domain.commands.DeleteRatingCommand;
import com.parkvision.iam.domain.commands.UpdateRatingCommand;
import com.parkvision.iam.domain.exceptions.RatingNotFoundException;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.domain.model.entities.Rating;
import com.parkvision.iam.domain.model.entities.RatingId;
import com.parkvision.iam.infrastructure.persistence.RatingRepository;
import com.parkvision.iam.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingCommandService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Transactional
    public Rating handle(CreateRatingCommand command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        Rating rating = new Rating(user, command.zoneId(), command.stars(), command.comment(), command.type());
        return ratingRepository.save(rating);
    }

    @Transactional
    public Optional<Rating> handle(UpdateRatingCommand command) {
        RatingId id = new RatingId(command.userId(), command.zoneId());
        return ratingRepository.findById(id).map(rating -> {
            rating.update(command);
            return ratingRepository.save(rating);
        });
    }

    @Transactional
    public void handle(DeleteRatingCommand command) {
        RatingId id = new RatingId(command.userId(), command.zoneId());
        if (!ratingRepository.existsById(id)) {
            throw new RatingNotFoundException(command.userId(), command.zoneId());
        }
        ratingRepository.deleteById(id);
    }
}
