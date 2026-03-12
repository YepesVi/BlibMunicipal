package com.Biblioteca.MunicipalBack.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.Biblioteca.MunicipalBack.auth.dto.AuthResponse;
import com.Biblioteca.MunicipalBack.auth.dto.LoginRequest;
import com.Biblioteca.MunicipalBack.auth.dto.LogoutRequest;
import com.Biblioteca.MunicipalBack.auth.dto.RefreshTokenRequest;
import com.Biblioteca.MunicipalBack.auth.service.AuthService;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
    }
}