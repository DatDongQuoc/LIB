package com.mycompany.user.service;

import com.mycompany.user.dto.RolePermissionDTO;
import com.mycompany.user.entity.RolePermission;
import com.mycompany.user.exception.DataNotFoundException;

import java.util.List;

public interface IRolePermissionService {
    List<RolePermission> findAllByRoleId(Long roleId);
    RolePermission addRolePermissionToRole(RolePermissionDTO rolePermissionDTO) throws DataNotFoundException;

}
