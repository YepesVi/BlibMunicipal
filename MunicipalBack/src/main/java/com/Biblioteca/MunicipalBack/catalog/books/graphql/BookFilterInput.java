package com.Biblioteca.MunicipalBack.catalog.books.graphql;

public record BookFilterInput(
        String title,
        Long authorId,
        Long categoryId,
        Integer publicationYear,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir
) {
    public BookFilterInput {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sortBy == null) sortBy = "title";
        if (sortDir == null) sortDir = "asc";
    }
}
