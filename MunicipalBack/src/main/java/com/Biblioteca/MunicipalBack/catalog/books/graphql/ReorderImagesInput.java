package com.Biblioteca.MunicipalBack.catalog.books.graphql;

import java.util.List;

public record ReorderImagesInput(
        List<ReorderImageItemInput> items
) {}
