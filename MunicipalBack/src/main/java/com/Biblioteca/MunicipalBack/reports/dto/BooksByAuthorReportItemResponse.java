package com.Biblioteca.MunicipalBack.reports.dto;

public record BooksByAuthorReportItemResponse(
        Long bookId,
        String isbn,
        String title,
        String publisher,
        Integer publicationYear,
        String categoryName,
        String primaryImageUrl
) {
}
