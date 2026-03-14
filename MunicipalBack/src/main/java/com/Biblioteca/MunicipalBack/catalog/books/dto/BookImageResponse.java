package com.Biblioteca.MunicipalBack.catalog.books.dto;

public record BookImageResponse(
        Long id,
        Long mediaAssetId,
        String secureUrl,
        boolean primaryImage,
        Integer sortOrder,
        String altText
) {
}