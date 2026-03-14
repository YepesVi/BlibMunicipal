package com.Biblioteca.MunicipalBack.catalog.books.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String publisher,
        Integer publicationYear,
        String description,
        Long authorId,
        String authorName,
        Long categoryId,
        String categoryName,
        String primaryImageUrl,
        List<BookImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
