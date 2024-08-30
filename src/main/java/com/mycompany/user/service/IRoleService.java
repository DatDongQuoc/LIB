package com.mycompany.user.service;

import com.mycompany.user.dto.RoleDto;
import com.mycompany.user.entity.Role;
import com.mycompany.user.exception.DataNotFoundException;

import java.util.List;

public interface IRoleService {
    List<Role> getRoles();
    Role getRoleById(Long roleId) throws DataNotFoundException;
    Role createRole(RoleDto roleDto) throws Exception;
    Role updateRole(Long roleId, RoleDto roleDto) throws DataNotFoundException;
    void deleteRole(Long roleId) throws DataNotFoundException;
}
