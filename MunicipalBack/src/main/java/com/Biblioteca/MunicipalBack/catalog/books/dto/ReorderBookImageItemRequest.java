package com.Biblioteca.MunicipalBack.catalog.books.dto;

import jakarta.validation.constraints.NotNull;

public record ReorderBookImageItemRequest(
        @NotNull(message = "Book image id is required")
        Long bookImageId,

        @NotNull(message = "Sort order is required")
        Integer sortOrder
) {
}