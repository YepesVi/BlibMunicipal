package com.Biblioteca.MunicipalBack.auth.dto;

import com.Biblioteca.MunicipalBack.shared.enums.UserRole;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long userId,
        String username,
        UserRole role
) {
}
