package com.mycompany.user.controller;

import com.mycompany.user.dto.request.AuthRequest;
import com.mycompany.user.dto.response.Response;
import com.mycompany.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mycompany.user.constant.ResponseCode.ERROR_CODE;
import static com.mycompany.user.constant.ResponseCode.SUCCESS_CODE;
import static com.mycompany.user.constant.ResponseMessage.LOGIN_FAILED;
import static com.mycompany.user.constant.ResponseMessage.LOGIN_SUCCESS;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/auth/login")
    public ResponseEntity<Response<?>> login(@RequestBody @Valid AuthRequest request) {
        try {
            return ResponseEntity.ok(
                    new Response<>(SUCCESS_CODE, LOGIN_SUCCESS, authService.authenticate(request)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new Response<>(ERROR_CODE, LOGIN_FAILED));
        }
    }
}
