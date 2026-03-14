package com.Biblioteca.MunicipalBack.catalog.books.dto;

public record BookFilterRequest(
        String title,
        Long authorId,
        Long categoryId,
        Integer publicationYear,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
