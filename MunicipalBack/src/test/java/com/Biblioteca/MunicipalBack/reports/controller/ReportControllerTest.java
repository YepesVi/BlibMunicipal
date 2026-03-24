package com.Biblioteca.MunicipalBack.reports.controller;

import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportItemResponse;
import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportResponse;
import com.Biblioteca.MunicipalBack.reports.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getBooksByAuthorIdCardReturnsJsonPreview() throws Exception {
        BooksByAuthorReportResponse response = new BooksByAuthorReportResponse(
                "0102",
                "Author Name",
                OffsetDateTime.parse("2026-03-20T10:15:30Z"),
                1,
                List.of(new BooksByAuthorReportItemResponse(
                        14L,
                        "9780307474728",
                        "One Hundred Years of Solitude",
                        "Harper",
                        1967,
                        "Novel",
                        "https://cdn.example.com/cover.jpg"
                ))
        );
        when(reportService.getBooksByAuthorIdCard("0102")).thenReturn(response);

        mockMvc.perform(get("/api/reports/books/by-author-id-card/0102"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.authorIdCard").value("0102"))
                .andExpect(jsonPath("$.authorName").value("Author Name"))
                .andExpect(jsonPath("$.totalBooks").value(1))
                .andExpect(jsonPath("$.books[0].title").value("One Hundred Years of Solitude"));
    }

    @Test
    void getBooksByAuthorIdCardPdfReturnsAttachment() throws Exception {
        byte[] pdfContent = "%PDF-mock".getBytes();
        when(reportService.getBooksByAuthorIdCardPdf("0102")).thenReturn(pdfContent);

        mockMvc.perform(get("/api/reports/books/by-author-id-card/0102/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"books-by-author-0102.pdf\""))
                .andExpect(content().bytes(pdfContent));
    }
}
