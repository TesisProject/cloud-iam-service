package com.parkvision.iam.interfaces.rest.internal;

import com.parkvision.iam.application.internal.IamEngagementFacade;
import com.parkvision.iam.application.internal.ZoneRatingView;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint interno (servicio-a-servicio) que expone las reseñas públicas de una zona como
 * {@link ZoneRatingView} (display name resuelto, email enmascarado). Lo consume {@code parking-service}
 * a través de su ACL ({@code IamEngagementAcl}) para componer el listado público de reseñas de una zona.
 * No se publica en el gateway; queda accesible solo dentro de la red de servicios.
 */
@Hidden
@RestController
@RequestMapping(value = "/api/v1/iam/internal/zones", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class IamEngagementInternalController {

    private final IamEngagementFacade iamEngagementFacade;

    @GetMapping("/{zoneId}/ratings")
    public ResponseEntity<List<ZoneRatingView>> getZoneRatings(@PathVariable Long zoneId) {
        return ResponseEntity.ok(iamEngagementFacade.getZoneRatings(zoneId));
    }
}
