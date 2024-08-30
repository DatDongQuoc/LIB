package com.mycompany.user.service.impl;

import com.mycompany.user.dto.RolePermissionDTO;
import com.mycompany.user.entity.Permission;
import com.mycompany.user.entity.Role;
import com.mycompany.user.entity.RolePermission;
import com.mycompany.user.exception.DataNotFoundException;
import com.mycompany.user.repository.PermissionRepository;
import com.mycompany.user.repository.RolePermissionRepository;
import com.mycompany.user.repository.RoleRepository;
import com.mycompany.user.service.IRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements IRolePermissionService {
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public List<RolePermission> findAllByRoleId(Long roleId) {
        List<RolePermission> permissions = rolePermissionRepository.findAllByRoleId(roleId);
        System.out.println("Fetched Permissions for Role ID " + roleId + ": " + permissions);
        return permissions;
    }

    @Transactional
    @Override
    public RolePermission addRolePermissionToRole(RolePermissionDTO rolePermissionDTO) throws DataNotFoundException {

        Role existRole = roleRepository
                .findById((long) Math.toIntExact(rolePermissionDTO.getRoleId()))
                .orElseThrow(() -> new DataNotFoundException("Cannot find role"));
        Permission existPermission = permissionRepository
                .findById(rolePermissionDTO.getPermissionId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find permission"));
        Optional<RolePermission> optionalRolePermission = rolePermissionRepository.findByRoleAndPermission(existRole, existPermission);
        if (optionalRolePermission.isPresent()) {
            RolePermission existRolePermission = optionalRolePermission.get();
            return rolePermissionRepository.save(existRolePermission);
        } else {
            RolePermission newRolePermission = new RolePermission();
            newRolePermission.setRole(existRole);
            newRolePermission.setPermission(existPermission);
            return rolePermissionRepository.save(newRolePermission);
        }
    }
}
