package com.Biblioteca.MunicipalBack.catalog.books.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderBookImagesRequest(
        @NotEmpty(message = "At least one reorder item is required")
        @Valid
        List<ReorderBookImageItemRequest> items
) {
}