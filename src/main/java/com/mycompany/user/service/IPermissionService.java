package com.mycompany.user.service;

import com.mycompany.user.dto.PermissionDTO;
import com.mycompany.user.entity.Permission;
import com.mycompany.user.exception.DataNotFoundException;

public interface IPermissionService {
    Permission createPermission(PermissionDTO permissionDTO);
    Permission updatePermission(PermissionDTO permissionDTO) throws DataNotFoundException;
    void deletePermission(Long permissionId) throws DataNotFoundException;
}
