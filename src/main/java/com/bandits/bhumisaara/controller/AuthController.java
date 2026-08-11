package com.bandits.bhumisaara.controller;

import com.bandits.bhumisaara.dto.request.LoginRequest;
import com.bandits.bhumisaara.dto.request.RegisterRequest;
import com.bandits.bhumisaara.dto.response.AuthResponse;
import com.bandits.bhumisaara.dto.response.TokenValidationResponse;
import com.bandits.bhumisaara.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody com.bandits.bhumisaara.dto.request.RefreshTokenRequest request) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authService.refreshToken(request));
    }

    // register / login / refresh / validate are permitAll in SecurityConfig;
    // /me is the one endpoint here that needs a session.
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authService.getCurrentUser());
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "token", required = false) String token) {
        String tokenToValidate = authorizationHeader != null ? authorizationHeader : token;

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authService.validateToken(tokenToValidate));
    }
}
