package com.Biblioteca.MunicipalBack.reports.graphql;

import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportItemResponse;
import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportResponse;
import com.Biblioteca.MunicipalBack.reports.service.ReportService;
import com.Biblioteca.MunicipalBack.shared.graphql.GraphQlExceptionResolver;
import com.Biblioteca.MunicipalBack.shared.graphql.GraphQlScalarConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@GraphQlTest(ReportGraphQlController.class)
@Import({GraphQlScalarConfig.class, GraphQlExceptionResolver.class})
class ReportGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private ReportService reportService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void booksByAuthorReport_returnsReport() {
        var report = new BooksByAuthorReportResponse(
                "0102", "Gabriel García Márquez",
                OffsetDateTime.parse("2026-03-20T10:15:30Z"),
                1,
                List.of(new BooksByAuthorReportItemResponse(
                        14L, "9780307474728", "One Hundred Years of Solitude",
                        "Harper", 1967, "Novel", "https://cdn.example.com/cover.jpg"
                ))
        );

        when(reportService.getBooksByAuthorIdCard("0102")).thenReturn(report);

        graphQlTester.document("""
                        query {
                            booksByAuthorReport(authorIdCard: "0102") {
                                authorIdCard authorName totalBooks
                                books { bookId isbn title publisher publicationYear categoryName }
                            }
                        }
                        """)
                .execute()
                .path("booksByAuthorReport.authorName").entity(String.class).isEqualTo("Gabriel García Márquez")
                .path("booksByAuthorReport.totalBooks").entity(Integer.class).isEqualTo(1)
                .path("booksByAuthorReport.books[0].title").entity(String.class).isEqualTo("One Hundred Years of Solitude");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void booksByAuthorReport_allowedForEmployee() {
        var report = new BooksByAuthorReportResponse(
                "0102", "Author", OffsetDateTime.now(), 0, List.of());

        when(reportService.getBooksByAuthorIdCard("0102")).thenReturn(report);

        graphQlTester.document("""
                        query {
                            booksByAuthorReport(authorIdCard: "0102") {
                                authorIdCard totalBooks
                            }
                        }
                        """)
                .execute()
                .path("booksByAuthorReport.authorIdCard").entity(String.class).isEqualTo("0102");
    }
}
