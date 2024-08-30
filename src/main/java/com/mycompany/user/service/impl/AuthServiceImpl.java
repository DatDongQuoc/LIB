package com.mycompany.user.service.impl;

import com.mycompany.user.dto.request.AuthRequest;
import com.mycompany.user.dto.response.AuthResponse;
import com.mycompany.user.entity.User;
import com.mycompany.user.jwt.JwtTokenUtil;
import com.mycompany.user.repository.UserRepository;
import com.mycompany.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            String accessToken = jwtTokenUtil.generateAccessToken(user);
            return new AuthResponse(user.getEmail(), accessToken);

        } catch (BadCredentialsException ex) {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
