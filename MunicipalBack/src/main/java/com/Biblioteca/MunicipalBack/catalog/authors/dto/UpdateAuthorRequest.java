package com.Biblioteca.MunicipalBack.catalog.authors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAuthorRequest(

        @NotBlank(message = "Author id card is required")
        @Size(min = 5, max = 30, message = "Author id card must be between 5 and 30 characters")
        String idCard,

        @NotBlank(message = "Author full name is required")
        @Size(min = 3, max = 150, message = "Author full name must be between 3 and 150 characters")
        String fullName,

        @NotBlank(message = "Author nationality is required")
        @Size(min = 2, max = 80, message = "Author nationality must be between 2 and 80 characters")
        String nationality,

        @Size(max = 2000, message = "Biography must not exceed 2000 characters")
        String biography
) {
}