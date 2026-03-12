package com.Biblioteca.MunicipalBack.auth.service;

import com.Biblioteca.MunicipalBack.auth.dto.JwtResponse;
import com.Biblioteca.MunicipalBack.auth.dto.LoginRequest;

public interface AuthService {
    JwtResponse login(LoginRequest request);
}
