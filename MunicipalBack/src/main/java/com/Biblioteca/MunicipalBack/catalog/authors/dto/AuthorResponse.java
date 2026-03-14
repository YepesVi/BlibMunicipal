package com.Biblioteca.MunicipalBack.catalog.authors.dto;

import java.time.LocalDateTime;

public record AuthorResponse(
        Long id,
        String idCard,
        String fullName,
        String nationality,
        String biography,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}