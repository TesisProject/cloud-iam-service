package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.UserProfileCommandService;
import com.parkvision.iam.application.queryservices.UserProfileQueryService;
import com.parkvision.iam.domain.commands.UpdateUserProfileCommand;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.domain.queries.GetUserProfileQuery;
import com.parkvision.iam.interfaces.assembler.UserProfileResourceAssembler;
import com.parkvision.iam.interfaces.resource.UpdateUserProfileRequest;
import com.parkvision.iam.interfaces.resource.UserProfileResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/iam/users/{userId}/profile", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Gestión del perfil de usuario")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserProfileCommandService userProfileCommandService;
    private final UserProfileQueryService userProfileQueryService;
    private final UserProfileResourceAssembler assembler;

    @GetMapping
    @PreAuthorize("#userId.toString() == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "Obtener perfil del usuario")
    public ResponseEntity<UserProfileResource> getProfile(@PathVariable Long userId) {
        var profile = userProfileQueryService.handle(new GetUserProfileQuery(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(assembler.toResource(profile));
    }

    @PutMapping
    @PreAuthorize("#userId.toString() == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "Actualizar perfil del usuario")
    public ResponseEntity<UserProfileResource> updateProfile(@PathVariable Long userId,
                                                             @Valid @RequestBody UpdateUserProfileRequest request) {
        var command = new UpdateUserProfileCommand(userId, request.firstName(), request.lastName(),
                request.phone(), request.avatarUrl(), request.bio());
        var profile = userProfileCommandService.handle(command);
        return ResponseEntity.ok(assembler.toResource(profile));
    }
}
