package com.parkvision.iam.infrastructure.persistence;

import com.parkvision.iam.domain.model.entities.RolePermission;
import com.parkvision.iam.domain.model.entities.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    List<RolePermission> findById_RoleId(Long roleId);
}
