package com.parkvision.iam.application.queryservices;

import com.parkvision.iam.domain.model.aggregates.Role;
import com.parkvision.iam.domain.model.entities.RolePermission;
import com.parkvision.iam.domain.queries.GetAllRolesQuery;
import com.parkvision.iam.domain.queries.GetRolePermissionsQuery;
import com.parkvision.iam.domain.queries.GetRoleQuery;
import com.parkvision.iam.infrastructure.persistence.RolePermissionRepository;
import com.parkvision.iam.infrastructure.persistence.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleQueryService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll();
    }

    public Optional<Role> handle(GetRoleQuery query) {
        return roleRepository.findById(query.roleId());
    }

    public List<RolePermission> handle(GetRolePermissionsQuery query) {
        return rolePermissionRepository.findById_RoleId(query.roleId());
    }
}
