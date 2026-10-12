package com.librarybook.management.controller;

import com.librarybook.management.dto.ApiResponse;
import com.librarybook.management.dto.AuthRequest;
import com.librarybook.management.dto.RegisterRequest;
import com.librarybook.management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Registration successful", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Login successful", authService.login(request)));
    }
}
