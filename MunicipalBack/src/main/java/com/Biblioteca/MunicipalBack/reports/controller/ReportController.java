package com.Biblioteca.MunicipalBack.reports.controller;

import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportResponse;
import com.Biblioteca.MunicipalBack.reports.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/books/by-author-id-card/{idCard}")
    public BooksByAuthorReportResponse getBooksByAuthorIdCard(@PathVariable String idCard) {
        return reportService.getBooksByAuthorIdCard(idCard);
    }

    @GetMapping(value = "/books/by-author-id-card/{idCard}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getBooksByAuthorIdCardPdf(@PathVariable String idCard) {
        byte[] content = reportService.getBooksByAuthorIdCardPdf(idCard);
        String filename = "books-by-author-" + idCard + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }
}
