package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.RoleCommandService;
import com.parkvision.iam.application.queryservices.RoleQueryService;
import com.parkvision.iam.domain.commands.AssignPermissionToRoleCommand;
import com.parkvision.iam.domain.commands.CreateRoleCommand;
import com.parkvision.iam.domain.commands.DeleteRoleCommand;
import com.parkvision.iam.domain.commands.RemovePermissionFromRoleCommand;
import com.parkvision.iam.domain.commands.UpdateRoleCommand;
import com.parkvision.iam.domain.exceptions.RoleNotFoundException;
import com.parkvision.iam.domain.queries.GetAllRolesQuery;
import com.parkvision.iam.domain.queries.GetRolePermissionsQuery;
import com.parkvision.iam.domain.queries.GetRoleQuery;
import com.parkvision.iam.interfaces.assembler.PermissionResourceAssembler;
import com.parkvision.iam.interfaces.assembler.RoleResourceAssembler;
import com.parkvision.iam.interfaces.resource.AssignPermissionRequest;
import com.parkvision.iam.interfaces.resource.CreateRoleRequest;
import com.parkvision.iam.interfaces.resource.PermissionResource;
import com.parkvision.iam.interfaces.resource.RoleResource;
import com.parkvision.iam.interfaces.resource.UpdateRoleRequest;
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
@RequestMapping(value = "/api/v1/iam/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles y asignación de permisos")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleCommandService roleCommandService;
    private final RoleQueryService roleQueryService;
    private final RoleResourceAssembler roleResourceAssembler;
    private final PermissionResourceAssembler permissionResourceAssembler;

    @PostMapping
    @Operation(summary = "Crear un nuevo rol")
    public ResponseEntity<RoleResource> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Long roleId = roleCommandService.handle(new CreateRoleCommand(request.name(), request.description()));
        var role = roleQueryService.handle(new GetRoleQuery(roleId))
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(roleId).toUri();
        return ResponseEntity.created(location).body(roleResourceAssembler.toResource(role));
    }

    @GetMapping
    @Operation(summary = "Listar todos los roles")
    public ResponseEntity<List<RoleResource>> getAllRoles() {
        var roles = roleQueryService.handle(new GetAllRolesQuery())
                .stream().map(roleResourceAssembler::toResource).toList();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Obtener un rol por id")
    public ResponseEntity<RoleResource> getRoleById(@PathVariable Long roleId) {
        var role = roleQueryService.handle(new GetRoleQuery(roleId))
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        return ResponseEntity.ok(roleResourceAssembler.toResource(role));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Actualizar un rol")
    public ResponseEntity<RoleResource> updateRole(@PathVariable Long roleId,
                                                   @Valid @RequestBody UpdateRoleRequest request) {
        var role = roleCommandService.handle(new UpdateRoleCommand(roleId, request.name(), request.description()))
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        return ResponseEntity.ok(roleResourceAssembler.toResource(role));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Eliminar un rol")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleCommandService.handle(new DeleteRoleCommand(roleId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/permissions")
    @Operation(summary = "Asignar un permiso a un rol")
    public ResponseEntity<Void> assignPermission(@PathVariable Long roleId,
                                                 @Valid @RequestBody AssignPermissionRequest request) {
        roleCommandService.handle(new AssignPermissionToRoleCommand(roleId, request.permissionId()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Quitar un permiso de un rol")
    public ResponseEntity<Void> removePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        roleCommandService.handle(new RemovePermissionFromRoleCommand(roleId, permissionId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Listar los permisos asignados a un rol")
    public ResponseEntity<List<PermissionResource>> getRolePermissions(@PathVariable Long roleId) {
        var permissions = roleQueryService.handle(new GetRolePermissionsQuery(roleId))
                .stream().map(rp -> permissionResourceAssembler.toResource(rp.getPermission())).toList();
        return ResponseEntity.ok(permissions);
    }
}
