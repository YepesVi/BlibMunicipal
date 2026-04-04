package com.Biblioteca.MunicipalBack.reports.graphql;

import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportResponse;
import com.Biblioteca.MunicipalBack.reports.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ReportGraphQlController {

    private final ReportService reportService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public BooksByAuthorReportResponse booksByAuthorReport(@Argument String authorIdCard) {
        return reportService.getBooksByAuthorIdCard(authorIdCard);
    }
}
