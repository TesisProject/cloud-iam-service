package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.UserPreferencesCommandService;
import com.parkvision.iam.application.queryservices.UserPreferencesQueryService;
import com.parkvision.iam.domain.commands.UpdateUserPreferencesCommand;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.domain.queries.GetUserPreferencesQuery;
import com.parkvision.iam.interfaces.assembler.UserPreferencesResourceAssembler;
import com.parkvision.iam.interfaces.resource.UpdateUserPreferencesRequest;
import com.parkvision.iam.interfaces.resource.UserPreferencesResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/iam/users/{userId}/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "User Preferences", description = "Gestión de preferencias del usuario")
@SecurityRequirement(name = "bearerAuth")
public class UserPreferencesController {

    private final UserPreferencesCommandService userPreferencesCommandService;
    private final UserPreferencesQueryService userPreferencesQueryService;
    private final UserPreferencesResourceAssembler assembler;

    @GetMapping
    @Operation(summary = "Obtener preferencias del usuario")
    public ResponseEntity<UserPreferencesResource> getPreferences(@PathVariable Long userId) {
        var preferences = userPreferencesQueryService.handle(new GetUserPreferencesQuery(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(assembler.toResource(preferences));
    }

    @PutMapping
    @Operation(summary = "Actualizar preferencias del usuario")
    public ResponseEntity<UserPreferencesResource> updatePreferences(@PathVariable Long userId,
                                                                     @Valid @RequestBody UpdateUserPreferencesRequest request) {
        var command = new UpdateUserPreferencesCommand(userId, request.darkMode(), request.language(),
                request.alertFreeSpace(), request.alertSaturated(), request.alertCameraFailure(), request.alertRadiusM());
        var preferences = userPreferencesCommandService.handle(command);
        return ResponseEntity.ok(assembler.toResource(preferences));
    }
}
