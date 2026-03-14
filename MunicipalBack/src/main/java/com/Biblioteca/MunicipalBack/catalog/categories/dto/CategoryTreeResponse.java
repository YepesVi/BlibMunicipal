package com.Biblioteca.MunicipalBack.catalog.categories.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CategoryTreeResponse> children
) {
}
