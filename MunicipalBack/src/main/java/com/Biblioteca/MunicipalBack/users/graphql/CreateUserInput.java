package com.Biblioteca.MunicipalBack.users.graphql;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserInput(
        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 80, message = "Username must be between 4 and 80 characters")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username can only contain letters, numbers, dot, underscore and hyphen"
        )
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotNull(message = "Role is required")
        String role
) {}
