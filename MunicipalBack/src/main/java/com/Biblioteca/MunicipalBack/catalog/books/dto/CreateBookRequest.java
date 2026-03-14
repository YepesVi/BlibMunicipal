package com.Biblioteca.MunicipalBack.catalog.books.dto;

import jakarta.validation.constraints.*;

public record CreateBookRequest(

        @NotBlank(message = "ISBN is required")
        @Size(min = 5, max = 30, message = "ISBN must be between 5 and 30 characters")
        String isbn,

        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
        String title,

        @NotBlank(message = "Publisher is required")
        @Size(min = 2, max = 150, message = "Publisher must be between 2 and 150 characters")
        String publisher,

        @NotNull(message = "Publication year is required")
        @Min(value = 1000, message = "Publication year must be valid")
        @Max(value = 9999, message = "Publication year must be valid")
        Integer publicationYear,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Author id is required")
        Long authorId,

        @NotNull(message = "Category id is required")
        Long categoryId
) {
}
