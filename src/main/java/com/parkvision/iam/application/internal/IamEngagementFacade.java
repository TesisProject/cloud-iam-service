package com.parkvision.iam.application.internal;

import com.parkvision.iam.domain.model.entities.UserProfile;
import com.parkvision.iam.infrastructure.persistence.RatingRepository;
import com.parkvision.iam.infrastructure.persistence.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Open-host service del contexto {@code iam}: API pública consumible por otros bounded contexts
 * (p.ej. {@code parking}) sin que estos conozcan el modelo interno de {@code iam}. Expone las reseñas
 * de una zona como {@link ZoneRatingView} (DTO plano), resolviendo aquí dentro el nombre a mostrar a
 * partir de {@code UserProfile} y enmascarando el email cuando no hay perfil. Nunca devuelve entidades
 * {@code Rating}/{@code UserProfile}/{@code User}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IamEngagementFacade {

    private final RatingRepository ratingRepository;
    private final UserProfileRepository userProfileRepository;

    /** Reseñas públicas de una zona, listas para presentación. */
    public List<ZoneRatingView> getZoneRatings(Long zoneId) {
        return ratingRepository.findById_ZoneId(zoneId).stream()
                .map(rating -> {
                    Long userId = rating.getId().getUserId();
                    UserProfile profile = userProfileRepository.findById(userId).orElse(null);
                    String displayName = resolveDisplayName(profile, rating.getUser().getEmail());
                    return new ZoneRatingView(
                            userId,
                            displayName,
                            rating.getStars(),
                            rating.getComment(),
                            rating.getType(),
                            rating.getCreatedAt());
                })
                .toList();
    }

    private String resolveDisplayName(UserProfile profile, String email) {
        if (profile != null
                && profile.getFirstName() != null
                && !profile.getFirstName().isBlank()) {
            String firstName = profile.getFirstName();
            String lastName = profile.getLastName();
            if (lastName != null && !lastName.isBlank()) {
                return firstName + " " + lastName.charAt(0) + ".";
            }
            return firstName;
        }
        return maskEmail(email);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at < 2) return "***" + (at >= 0 ? email.substring(at) : email);
        return email.substring(0, 2) + "***" + email.substring(at);
    }
}
