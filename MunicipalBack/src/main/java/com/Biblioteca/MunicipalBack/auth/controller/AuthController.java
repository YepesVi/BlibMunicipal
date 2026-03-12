package com.Biblioteca.MunicipalBack.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.Biblioteca.MunicipalBack.auth.dto.JwtResponse;
import com.Biblioteca.MunicipalBack.auth.dto.LoginRequest;
import com.Biblioteca.MunicipalBack.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}