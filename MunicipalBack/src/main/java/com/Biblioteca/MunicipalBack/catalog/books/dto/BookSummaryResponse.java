package com.Biblioteca.MunicipalBack.catalog.books.dto;

import java.time.LocalDateTime;

public record BookSummaryResponse(
        Long id,
        String isbn,
        String title,
        String publisher,
        Integer publicationYear,
        Long authorId,
        String authorName,
        Long categoryId,
        String categoryName,
        String primaryImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}