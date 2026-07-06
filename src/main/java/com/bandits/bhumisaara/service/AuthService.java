package com.bhumisaara.service;

import com.bhumisaara.dto.request.LoginRequest;
import com.bhumisaara.dto.request.RegisterRequest;
import com.bhumisaara.dto.response.AuthResponse;
import com.bhumisaara.dto.response.TokenValidationResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse getCurrentUser();

    TokenValidationResponse validateToken(String token);

    AuthResponse refreshToken(com.bhumisaara.dto.request.RefreshTokenRequest request);
}
