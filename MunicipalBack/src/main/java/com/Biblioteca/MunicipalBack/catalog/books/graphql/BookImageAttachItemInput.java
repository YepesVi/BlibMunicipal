package com.Biblioteca.MunicipalBack.catalog.books.graphql;

public record BookImageAttachItemInput(
        Long mediaAssetId,
        Boolean primaryImage,
        String altText
) {}
