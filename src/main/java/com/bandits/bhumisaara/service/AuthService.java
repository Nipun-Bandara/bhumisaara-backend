package com.bandits.bhumisaara.service;

import com.bandits.bhumisaara.dto.request.LoginRequest;
import com.bandits.bhumisaara.dto.request.RegisterRequest;
import com.bandits.bhumisaara.dto.response.AuthResponse;
import com.bandits.bhumisaara.dto.response.TokenValidationResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse getCurrentUser();

    TokenValidationResponse validateToken(String token);

    AuthResponse refreshToken(com.bandits.bhumisaara.dto.request.RefreshTokenRequest request);
}
