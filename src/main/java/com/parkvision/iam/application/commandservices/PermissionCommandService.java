package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.CreatePermissionCommand;
import com.parkvision.iam.domain.commands.DeletePermissionCommand;
import com.parkvision.iam.domain.commands.UpdatePermissionCommand;
import com.parkvision.iam.domain.exceptions.PermissionNotFoundException;
import com.parkvision.iam.domain.model.entities.Permission;
import com.parkvision.iam.infrastructure.persistence.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionCommandService {

    private final PermissionRepository permissionRepository;

    @Transactional
    public Long handle(CreatePermissionCommand command) {
        Permission permission = new Permission(command.name(), command.resource(), command.action(), command.description());
        return permissionRepository.save(permission).getId();
    }

    @Transactional
    public Optional<Permission> handle(UpdatePermissionCommand command) {
        return permissionRepository.findById(command.permissionId()).map(permission -> {
            permission.updateInfo(command.name(), command.resource(), command.action(), command.description());
            return permissionRepository.save(permission);
        });
    }

    @Transactional
    public void handle(DeletePermissionCommand command) {
        if (!permissionRepository.existsById(command.permissionId())) {
            throw new PermissionNotFoundException(command.permissionId());
        }
        permissionRepository.deleteById(command.permissionId());
    }
}
