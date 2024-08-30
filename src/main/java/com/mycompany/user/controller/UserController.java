package com.mycompany.user.controller;

import com.mycompany.user.dto.UserDto;
import com.mycompany.user.dto.request.UserPageRequest;
import com.mycompany.user.dto.request.UserUpdateRequest;
import com.mycompany.user.dto.response.UserPageResponse;
import com.mycompany.user.entity.User;
import com.mycompany.user.exception.CustomException;
import com.mycompany.user.exception.UserNotFoundException;
import com.mycompany.user.dto.response.Response;
import com.mycompany.user.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static com.mycompany.user.constant.ResponseCode.*;
import static com.mycompany.user.constant.ResponseMessage.*;

@RestController
@Controller
public class UserController {

    @Autowired
    private UserServiceImpl service;

    @PostMapping("/users/page")
    public ResponseEntity<Response<UserPageResponse>> listByPage(@RequestBody UserPageRequest request) {
        return ResponseEntity.ok(
                new Response<>(SUCCESS_CODE, USER_RETRIEVED, service.getUserListByPage(request)));
    }

    @PostMapping("/users/create")
    public ResponseEntity<Response<String>> saveUser(@Valid @RequestBody UserDto userDTO) {
        try {
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, USER_SAVED, service.save(userDTO)));
        } catch (CustomException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PatchMapping("/users/update")
    public ResponseEntity<Response<User>> updateUser(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        try {
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, USER_UPDATED, service.update(userUpdateRequest)));
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, USER_NOT_FOUND));
        } catch (CustomException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/delete")
    public ResponseEntity<Response<String>> deleteUser(@RequestParam Long id) {
        try {
            service.delete((long) Math.toIntExact(id));
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, "The user ID " + id + " has been deleted."));
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/users/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadUser(@RequestPart("file") MultipartFile file) {
        try {
            if (service.uploadUser(file) == 0) {
                return ResponseEntity.badRequest().body(
                        Files.readString(Paths.get(Objects.requireNonNull(file.getOriginalFilename()))));
            }
            return ResponseEntity.ok().body(
                    new Response<>(SUCCESS_CODE, UPLOAD_SUCCESSFULLY));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new Response<>(ERROR_CODE, UPLOAD_FAILED));
        }
    }

    @GetMapping("/users/export")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        service.exportUsersToCSV(response);
        }
}


