package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.AssignPermissionToRoleCommand;
import com.parkvision.iam.domain.commands.CreateRoleCommand;
import com.parkvision.iam.domain.commands.DeleteRoleCommand;
import com.parkvision.iam.domain.commands.RemovePermissionFromRoleCommand;
import com.parkvision.iam.domain.commands.UpdateRoleCommand;
import com.parkvision.iam.domain.exceptions.PermissionNotFoundException;
import com.parkvision.iam.domain.exceptions.RoleNotFoundException;
import com.parkvision.iam.domain.model.aggregates.Role;
import com.parkvision.iam.domain.model.entities.RolePermission;
import com.parkvision.iam.domain.model.entities.RolePermissionId;
import com.parkvision.iam.infrastructure.persistence.PermissionRepository;
import com.parkvision.iam.infrastructure.persistence.RolePermissionRepository;
import com.parkvision.iam.infrastructure.persistence.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleCommandService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    public Long handle(CreateRoleCommand command) {
        Role role = new Role(command.name(), command.description());
        return roleRepository.save(role).getId();
    }

    @Transactional
    public Optional<Role> handle(UpdateRoleCommand command) {
        return roleRepository.findById(command.roleId()).map(role -> {
            role.updateInfo(command.name(), command.description());
            return roleRepository.save(role);
        });
    }

    @Transactional
    public void handle(DeleteRoleCommand command) {
        if (!roleRepository.existsById(command.roleId())) {
            throw new RoleNotFoundException(command.roleId());
        }
        roleRepository.deleteById(command.roleId());
    }

    @Transactional
    public void handle(AssignPermissionToRoleCommand command) {
        Role role = roleRepository.findById(command.roleId())
                .orElseThrow(() -> new RoleNotFoundException(command.roleId()));
        var permission = permissionRepository.findById(command.permissionId())
                .orElseThrow(() -> new PermissionNotFoundException(command.permissionId()));
        rolePermissionRepository.save(new RolePermission(role, permission));
    }

    @Transactional
    public void handle(RemovePermissionFromRoleCommand command) {
        RolePermissionId id = new RolePermissionId(command.roleId(), command.permissionId());
        if (!rolePermissionRepository.existsById(id)) {
            throw new RoleNotFoundException(command.roleId());
        }
        rolePermissionRepository.deleteById(id);
    }
}
