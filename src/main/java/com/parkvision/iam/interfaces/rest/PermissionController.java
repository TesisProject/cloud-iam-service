package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.PermissionCommandService;
import com.parkvision.iam.application.queryservices.PermissionQueryService;
import com.parkvision.iam.domain.commands.CreatePermissionCommand;
import com.parkvision.iam.domain.commands.DeletePermissionCommand;
import com.parkvision.iam.domain.commands.UpdatePermissionCommand;
import com.parkvision.iam.domain.exceptions.PermissionNotFoundException;
import com.parkvision.iam.domain.queries.GetAllPermissionsQuery;
import com.parkvision.iam.domain.queries.GetPermissionQuery;
import com.parkvision.iam.interfaces.assembler.PermissionResourceAssembler;
import com.parkvision.iam.interfaces.resource.CreatePermissionRequest;
import com.parkvision.iam.interfaces.resource.PermissionResource;
import com.parkvision.iam.interfaces.resource.UpdatePermissionRequest;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/iam/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Gestión de permisos del sistema")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionCommandService permissionCommandService;
    private final PermissionQueryService permissionQueryService;
    private final PermissionResourceAssembler assembler;

    @PostMapping
    @Operation(summary = "Crear un nuevo permiso")
    public ResponseEntity<PermissionResource> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        Long permissionId = permissionCommandService.handle(
                new CreatePermissionCommand(request.name(), request.resource(), request.action(), request.description()));
        var permission = permissionQueryService.handle(new GetPermissionQuery(permissionId))
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(permissionId).toUri();
        return ResponseEntity.created(location).body(assembler.toResource(permission));
    }

    @GetMapping
    @Operation(summary = "Listar todos los permisos")
    public ResponseEntity<List<PermissionResource>> getAllPermissions() {
        var permissions = permissionQueryService.handle(new GetAllPermissionsQuery())
                .stream().map(assembler::toResource).toList();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{permissionId}")
    @Operation(summary = "Obtener un permiso por id")
    public ResponseEntity<PermissionResource> getPermissionById(@PathVariable Long permissionId) {
        var permission = permissionQueryService.handle(new GetPermissionQuery(permissionId))
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
        return ResponseEntity.ok(assembler.toResource(permission));
    }

    @PutMapping("/{permissionId}")
    @Operation(summary = "Actualizar un permiso")
    public ResponseEntity<PermissionResource> updatePermission(@PathVariable Long permissionId,
                                                               @Valid @RequestBody UpdatePermissionRequest request) {
        var permission = permissionCommandService.handle(
                new UpdatePermissionCommand(permissionId, request.name(), request.resource(), request.action(), request.description()))
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
        return ResponseEntity.ok(assembler.toResource(permission));
    }

    @DeleteMapping("/{permissionId}")
    @Operation(summary = "Eliminar un permiso")
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        permissionCommandService.handle(new DeletePermissionCommand(permissionId));
        return ResponseEntity.noContent().build();
    }
}
