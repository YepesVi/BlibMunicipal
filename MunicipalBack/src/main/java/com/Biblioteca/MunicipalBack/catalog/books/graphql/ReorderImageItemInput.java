package com.Biblioteca.MunicipalBack.catalog.books.graphql;

public record ReorderImageItemInput(
        Long bookImageId,
        Integer sortOrder
) {}
