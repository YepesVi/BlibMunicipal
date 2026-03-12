package com.Biblioteca.MunicipalBack.users.dto;

import com.Biblioteca.MunicipalBack.shared.enums.UserRole;

public record UserResponse(
        Long id,
        String username,
        UserRole role
) {
}
