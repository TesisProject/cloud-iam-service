package com.parkvision.iam.application.queryservices;

import com.parkvision.iam.domain.model.entities.Permission;
import com.parkvision.iam.domain.queries.GetAllPermissionsQuery;
import com.parkvision.iam.domain.queries.GetPermissionQuery;
import com.parkvision.iam.infrastructure.persistence.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionQueryService {

    private final PermissionRepository permissionRepository;

    public List<Permission> handle(GetAllPermissionsQuery query) {
        return permissionRepository.findAll();
    }

    public Optional<Permission> handle(GetPermissionQuery query) {
        return permissionRepository.findById(query.permissionId());
    }
}
