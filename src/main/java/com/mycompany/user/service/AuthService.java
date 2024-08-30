package com.mycompany.user.service;

import com.mycompany.user.dto.request.AuthRequest;
import com.mycompany.user.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
}
