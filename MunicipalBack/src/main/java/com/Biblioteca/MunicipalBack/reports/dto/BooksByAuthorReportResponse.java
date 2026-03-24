package com.Biblioteca.MunicipalBack.reports.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record BooksByAuthorReportResponse(
        String authorIdCard,
        String authorName,
        OffsetDateTime generatedAt,
        int totalBooks,
        List<BooksByAuthorReportItemResponse> books
) {
}
