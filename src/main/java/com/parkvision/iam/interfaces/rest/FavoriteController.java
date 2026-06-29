package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.FavoriteCommandService;
import com.parkvision.iam.application.queryservices.FavoriteQueryService;
import com.parkvision.iam.domain.commands.AddFavoriteCommand;
import com.parkvision.iam.domain.commands.RemoveFavoriteCommand;
import com.parkvision.iam.domain.queries.GetFavoritesByUserQuery;
import com.parkvision.iam.interfaces.assembler.FavoriteResourceAssembler;
import com.parkvision.iam.interfaces.resource.AddFavoriteRequest;
import com.parkvision.iam.interfaces.resource.FavoriteResource;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/iam/users/{userId}/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Gestión de zonas favoritas del usuario")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteCommandService favoriteCommandService;
    private final FavoriteQueryService favoriteQueryService;
    private final FavoriteResourceAssembler assembler;

    @PostMapping
    @Operation(summary = "Agregar una zona a favoritos")
    public ResponseEntity<Void> addFavorite(@PathVariable Long userId,
                                            @Valid @RequestBody AddFavoriteRequest request) {
        favoriteCommandService.handle(new AddFavoriteCommand(userId, request.zoneId()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar zonas favoritas del usuario")
    public ResponseEntity<List<FavoriteResource>> getFavorites(@PathVariable Long userId) {
        var favorites = favoriteQueryService.handle(new GetFavoritesByUserQuery(userId))
                .stream().map(assembler::toResource).toList();
        return ResponseEntity.ok(favorites);
    }

    @DeleteMapping("/{zoneId}")
    @Operation(summary = "Eliminar una zona de favoritos")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long zoneId) {
        favoriteCommandService.handle(new RemoveFavoriteCommand(userId, zoneId));
        return ResponseEntity.noContent().build();
    }
}
