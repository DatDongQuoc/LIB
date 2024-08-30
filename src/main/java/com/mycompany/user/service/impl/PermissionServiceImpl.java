package com.mycompany.user.service.impl;

import com.mycompany.user.dto.PermissionDTO;
import com.mycompany.user.entity.Permission;
import com.mycompany.user.exception.DataNotFoundException;
import com.mycompany.user.repository.PermissionRepository;
import com.mycompany.user.service.IPermissionService;
import com.mycompany.user.util.ConvertUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private final PermissionRepository permissionRepository;

    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Permission createPermission(PermissionDTO permissionDTO) {
        Permission newPermission = ConvertUtil.convertObject(permissionDTO, object -> modelMapper.map(object, Permission.class));
        return permissionRepository.save(newPermission);
    }

    @Transactional
    @Override
    public Permission updatePermission(PermissionDTO permissionDTO) throws DataNotFoundException {
        // Extract the ID from the DTO
        Long permissionId = permissionDTO.getId();

        // Fetch the existing permission by ID
        Permission existPermission = permissionRepository
                .findById(permissionId)
                .orElseThrow(() -> new DataNotFoundException("Cannot find permission"));
        ConvertUtil.convertObject(permissionDTO, object -> {
                    modelMapper.map(permissionDTO, existPermission);
                    return existPermission;
        });
        return permissionRepository.save(existPermission);
    }

    @Transactional
    @Override
    public void deletePermission(Long permissionId) throws DataNotFoundException {
        Permission existPermission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new DataNotFoundException("Cannot find permission with id:" + permissionId));
        permissionRepository.delete(existPermission);
    }
}
