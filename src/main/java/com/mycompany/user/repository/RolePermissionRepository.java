package com.mycompany.user.repository;

import com.mycompany.user.entity.Permission;
import com.mycompany.user.entity.Role;
import com.mycompany.user.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findAllByRoleId(Long roleId);
    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);
}
