package com.Biblioteca.MunicipalBack.catalog.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 120, message = "Category name must be between 2 and 120 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        Long parentId
) {
}