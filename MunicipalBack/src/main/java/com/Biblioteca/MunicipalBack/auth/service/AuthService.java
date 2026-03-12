package com.Biblioteca.MunicipalBack.auth.service;

import com.Biblioteca.MunicipalBack.auth.dto.AuthResponse;
import com.Biblioteca.MunicipalBack.auth.dto.LoginRequest;
import com.Biblioteca.MunicipalBack.auth.dto.LogoutRequest;
import com.Biblioteca.MunicipalBack.auth.dto.RefreshTokenRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(LogoutRequest request);

}
