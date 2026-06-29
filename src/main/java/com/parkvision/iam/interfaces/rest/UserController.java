package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.UserCommandService;
import com.parkvision.iam.application.queryservices.UserQueryService;
import com.parkvision.iam.domain.commands.ChangePasswordCommand;
import com.parkvision.iam.domain.commands.ChangeUserRoleCommand;
import com.parkvision.iam.domain.commands.SignUpCommand;
import com.parkvision.iam.domain.commands.ToggleUserStatusCommand;
import com.parkvision.iam.domain.commands.UpdateUserCommand;
import com.parkvision.iam.domain.exceptions.UserNotFoundException;
import com.parkvision.iam.domain.queries.GetAllUsersQuery;
import com.parkvision.iam.domain.queries.GetUserQuery;
import com.parkvision.iam.interfaces.assembler.UserResourceAssembler;
import com.parkvision.iam.interfaces.resource.ChangePasswordRequest;
import com.parkvision.iam.interfaces.resource.ChangeUserRoleRequest;
import com.parkvision.iam.interfaces.resource.SignUpRequest;
import com.parkvision.iam.interfaces.resource.ToggleUserStatusRequest;
import com.parkvision.iam.interfaces.resource.UpdateUserRequest;
import com.parkvision.iam.interfaces.resource.UserResource;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/iam/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Users", description = "Registro y consulta de usuarios")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserResourceAssembler userResourceAssembler;

    @PostMapping
    @Operation(summary = "Registrar un nuevo usuario")
    public ResponseEntity<UserResource> signUp(@Valid @RequestBody SignUpRequest request) {
        var command = new SignUpCommand(request.email(), request.password(), request.roleName());
        Long userId = userCommandService.handle(command);
        var user = userQueryService.handle(new GetUserQuery(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(userId).toUri();
        return ResponseEntity.created(location).body(userResourceAssembler.toResource(user));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("#userId.toString() == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por id", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var user = userQueryService.handle(new GetUserQuery(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(userResourceAssembler.toResource(user));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("#userId.toString() == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "Actualizar email o contraseña del usuario", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResource> updateUser(@PathVariable Long userId,
                                                   @Valid @RequestBody UpdateUserRequest request) {
        var user = userCommandService.handle(new UpdateUserCommand(userId, request.email(), request.newPassword()))
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(userResourceAssembler.toResource(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var users = userQueryService.handle(new GetAllUsersQuery())
                .stream().map(userResourceAssembler::toResource).toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar rol de un usuario (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResource> changeRole(@PathVariable Long userId,
                                                   @Valid @RequestBody ChangeUserRoleRequest request) {
        var user = userCommandService.handle(new ChangeUserRoleCommand(userId, request.roleName()))
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(userResourceAssembler.toResource(user));
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar/desactivar usuario (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResource> toggleStatus(@PathVariable Long userId,
                                                     @Valid @RequestBody ToggleUserStatusRequest request) {
        var user = userCommandService.handle(new ToggleUserStatusCommand(userId, request.active()))
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(userResourceAssembler.toResource(user));
    }

    @PutMapping("/{userId}/password")
    @PreAuthorize("#userId.toString() == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "Cambiar contraseña verificando la actual", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> changePassword(@PathVariable Long userId,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        userCommandService.handle(
                new ChangePasswordCommand(userId, request.currentPassword(), request.newPassword()));
        return ResponseEntity.ok().build();
    }
}
