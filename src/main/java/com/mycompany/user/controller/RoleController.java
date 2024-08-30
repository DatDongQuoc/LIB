package com.mycompany.user.controller;

import com.mycompany.user.dto.RoleDto;
import com.mycompany.user.dto.RolePermissionDTO;
import com.mycompany.user.dto.response.Response;
import com.mycompany.user.entity.Role;
import com.mycompany.user.entity.RolePermission;
import com.mycompany.user.service.IRolePermissionService;
import com.mycompany.user.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;
import static com.mycompany.user.constant.ResponseCode.SUCCESS_CODE;
import static com.mycompany.user.constant.ResponseMessage.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/role")
public class RoleController {

    private final IRoleService roleService;
    private final IRolePermissionService rolePermissionService;

    @GetMapping("get-roles")
    public ResponseEntity<Response<List<Role>>> getRoles(){
        try{
            List<Role> roles = roleService.getRoles();
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, ROLES_RETRIEVED, roles)
            );
        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage())
            );
        }
    }

    @PostMapping("insert-role")
    public ResponseEntity<Response<Role>> createRole(@RequestBody RoleDto roleDto){
        try{
            Role role = roleService.createRole(roleDto);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, ROLE_INSERTED, role)
            );
        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage())
            );
        }
    }

    @PutMapping("/update-role")
    public ResponseEntity<Response<Role>> updateRole(@RequestBody RoleDto roleDto, @RequestParam Long roleId){
        try{
            Role role = roleService.updateRole(Long.valueOf(roleId), roleDto);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, ROLE_UPDATED_SUCCESSFULLY, role)
            );
        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage())
            );
        }
    }

    @DeleteMapping("/delete-role")
    public ResponseEntity<Response<List<Role>>> deleteRole(@RequestParam("id") Long roleId){
        try{
            roleService.deleteRole((long) Math.toIntExact(roleId));
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, ROLE_DELETED_SUCCESSFULLY)
            );
        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage())
            );
        }
    }

    @PostMapping("/add-role-permission-to-role")
    public ResponseEntity<Response<RolePermission>> addRolePermission(@RequestBody RolePermissionDTO rolePermissionDTO){
        try{
        RolePermission rolePermission = rolePermissionService.addRolePermissionToRole(rolePermissionDTO);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, ADD_PERMISSON_TO_ROLE, rolePermission));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        }
    }
}
