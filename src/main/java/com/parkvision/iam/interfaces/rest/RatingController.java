package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.RatingCommandService;
import com.parkvision.iam.application.queryservices.RatingQueryService;
import com.parkvision.iam.domain.commands.CreateRatingCommand;
import com.parkvision.iam.domain.commands.DeleteRatingCommand;
import com.parkvision.iam.domain.commands.UpdateRatingCommand;
import com.parkvision.iam.domain.exceptions.RatingNotFoundException;
import com.parkvision.iam.domain.queries.GetRatingsByUserQuery;
import com.parkvision.iam.interfaces.assembler.RatingResourceAssembler;
import com.parkvision.iam.interfaces.resource.CreateRatingRequest;
import com.parkvision.iam.interfaces.resource.RatingResource;
import com.parkvision.iam.interfaces.resource.UpdateRatingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/iam/users/{userId}/ratings", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Gestión de calificaciones de zonas por usuario")
@SecurityRequirement(name = "bearerAuth")
public class RatingController {

    private final RatingCommandService ratingCommandService;
    private final RatingQueryService ratingQueryService;
    private final RatingResourceAssembler assembler;

    @PostMapping
    @Operation(summary = "Calificar una zona")
    public ResponseEntity<RatingResource> createRating(@PathVariable Long userId,
                                                       @Valid @RequestBody CreateRatingRequest request) {
        var rating = ratingCommandService.handle(
                new CreateRatingCommand(userId, request.zoneId(), request.stars(), request.comment(), request.type()));
        return ResponseEntity.ok(assembler.toResource(rating));
    }

    @GetMapping
    @Operation(summary = "Listar calificaciones del usuario")
    public ResponseEntity<List<RatingResource>> getRatings(@PathVariable Long userId) {
        var ratings = ratingQueryService.handle(new GetRatingsByUserQuery(userId))
                .stream().map(assembler::toResource).toList();
        return ResponseEntity.ok(ratings);
    }

    @PutMapping("/{zoneId}")
    @Operation(summary = "Actualizar calificación de una zona")
    public ResponseEntity<RatingResource> updateRating(@PathVariable Long userId, @PathVariable Long zoneId,
                                                       @Valid @RequestBody UpdateRatingRequest request) {
        var rating = ratingCommandService.handle(
                new UpdateRatingCommand(userId, zoneId, request.stars(), request.comment(), request.type()))
                .orElseThrow(() -> new RatingNotFoundException(userId, zoneId));
        return ResponseEntity.ok(assembler.toResource(rating));
    }

    @DeleteMapping("/{zoneId}")
    @Operation(summary = "Eliminar calificación de una zona")
    public ResponseEntity<Void> deleteRating(@PathVariable Long userId, @PathVariable Long zoneId) {
        ratingCommandService.handle(new DeleteRatingCommand(userId, zoneId));
        return ResponseEntity.noContent().build();
    }
}
