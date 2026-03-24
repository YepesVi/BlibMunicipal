package com.Biblioteca.MunicipalBack.reports.service;

import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportResponse;

public interface ReportService {

    BooksByAuthorReportResponse getBooksByAuthorIdCard(String idCard);

    byte[] getBooksByAuthorIdCardPdf(String idCard);
}
