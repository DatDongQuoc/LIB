package com.mycompany.user.controller;

import com.mycompany.user.dto.PermissionDTO;
import com.mycompany.user.dto.response.Response;
import com.mycompany.user.entity.Permission;
import com.mycompany.user.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mycompany.user.constant.ResponseCode.*;
import static com.mycompany.user.constant.ResponseMessage.*;

@RestController
@RequiredArgsConstructor

@RequestMapping("/permissions")
public class PermissionController {
    private final IPermissionService permissionService;

    @PostMapping("/insert-permission")
    public ResponseEntity<Response<Permission>> createPermission(@RequestBody PermissionDTO permissionDTO){
        try {
        return ResponseEntity.ok().body(
                new Response<>(CREATE_CODE, PERMISSION_INSERTED,permissionService.createPermission(permissionDTO)));

        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        }
    }

    @PutMapping("/update-permission")
    public ResponseEntity<Response<Permission>> updatePermission(@RequestBody PermissionDTO permissionDTO){
        try {
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, PERMISSION_UPDATED_SUCCESSFULLY,permissionService.updatePermission(permissionDTO)));

        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        }
    }

    @DeleteMapping("/delete-permission")
    public ResponseEntity<Response<Permission>> deletePermission(@RequestParam Long id){
        try {
            permissionService.deletePermission(id);
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, PERMISSION_DELETED_SUCCESSFULLY));

        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        }
    }
}
