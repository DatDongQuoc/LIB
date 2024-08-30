package com.mycompany.user.service.impl;

import com.mycompany.user.dto.RoleDto;
import com.mycompany.user.entity.Permission;
import com.mycompany.user.entity.Role;
import com.mycompany.user.entity.RolePermission;
import com.mycompany.user.exception.CustomException;
import com.mycompany.user.exception.DataNotFoundException;
import com.mycompany.user.filter.DynamicAuthorityFilter;
import com.mycompany.user.repository.PermissionRepository;
import com.mycompany.user.repository.RoleRepository;
import com.mycompany.user.service.IRoleService;
import com.mycompany.user.util.ConvertUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleById(Long roleId) throws DataNotFoundException {
        return roleRepository.findById((long) Math.toIntExact(roleId)).orElseThrow(
                () -> new DataNotFoundException(String.format("Cannot find role with id: %d", roleId))
        );
    }

    private static final Logger logger = LoggerFactory.getLogger(DynamicAuthorityFilter.class);

    @Transactional
    @Override
    public Role createRole(RoleDto roleDto) throws Exception {
        Role newRole = ConvertUtil.convertObject(roleDto,
                object -> modelMapper.map(object, Role.class)
        );
    // Debug logging before saving
        logger.info("Adding Role: ID = {}, Name = {}", newRole.getId(), newRole.getName());
        return roleRepository.save(newRole);
    }

    @Transactional
    @Override
    public Role updateRole(Long roleId, RoleDto roleDto) throws DataNotFoundException {
        Role existRole = roleRepository.findById((long) Math.toIntExact(roleId))
                .orElseThrow(() -> new DataNotFoundException(String.format("Cannot find role with id: %d", roleId)));
        roleDto.setName(roleDto.getName().toUpperCase());
        ConvertUtil.convertObject(
                roleDto, object -> {
                    modelMapper.map(object, existRole);
                    return existRole;
                }
        );
        return roleRepository.save(existRole);
    }

    @Transactional
    @Override
    public void deleteRole(Long roleId) throws DataNotFoundException {
        Role existRole = roleRepository.findById((long) Math.toIntExact(roleId))
                .orElseThrow(() -> new DataNotFoundException(String.format("Cannot find role with id: %d", roleId)));

        roleRepository.delete(existRole);
    }
}
