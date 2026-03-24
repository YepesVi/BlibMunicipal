package com.Biblioteca.MunicipalBack.reports.service;

import com.Biblioteca.MunicipalBack.catalog.authors.model.Author;
import com.Biblioteca.MunicipalBack.catalog.authors.repository.AuthorRepository;
import com.Biblioteca.MunicipalBack.catalog.books.model.Book;
import com.Biblioteca.MunicipalBack.catalog.books.model.BookImage;
import com.Biblioteca.MunicipalBack.catalog.books.repository.BookRepository;
import com.Biblioteca.MunicipalBack.catalog.categories.model.Category;
import com.Biblioteca.MunicipalBack.media.model.MediaAsset;
import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportResponse;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void getBooksByAuthorIdCardBuildsReportWithPrimaryImageUrl() {
        Author author = mock(Author.class);
        Category category = mock(Category.class);
        Book book = mock(Book.class);
        BookImage firstPrimary = mock(BookImage.class);
        BookImage secondPrimary = mock(BookImage.class);
        MediaAsset imageTwo = mock(MediaAsset.class);

        when(author.getId()).thenReturn(3L);
        when(author.getIdCard()).thenReturn("0102030405");
        when(author.getFullName()).thenReturn("Gabriel Garcia Marquez");

        when(category.getName()).thenReturn("Novel");

        when(imageTwo.getSecureUrl()).thenReturn("https://cdn.example.com/cover-b.jpg");

        when(firstPrimary.isPrimaryImage()).thenReturn(true);
        when(firstPrimary.getSortOrder()).thenReturn(2);
        when(secondPrimary.isPrimaryImage()).thenReturn(true);
        when(secondPrimary.getSortOrder()).thenReturn(1);
        when(secondPrimary.getMediaAsset()).thenReturn(imageTwo);

        when(book.getId()).thenReturn(10L);
        when(book.getIsbn()).thenReturn("9780307474728");
        when(book.getTitle()).thenReturn("One Hundred Years of Solitude");
        when(book.getPublisher()).thenReturn("Harper");
        when(book.getPublicationYear()).thenReturn(1967);
        when(book.getCategory()).thenReturn(category);
        when(book.getImages()).thenReturn(List.of(firstPrimary, secondPrimary));

        when(authorRepository.findByIdCard("0102030405")).thenReturn(Optional.of(author));
        when(bookRepository.findByAuthorIdOrderByTitleAsc(3L)).thenReturn(List.of(book));

        BooksByAuthorReportResponse report = reportService.getBooksByAuthorIdCard(" 0102030405 ");

        assertEquals("0102030405", report.authorIdCard());
        assertEquals("Gabriel Garcia Marquez", report.authorName());
        assertEquals(1, report.totalBooks());
        assertEquals("One Hundred Years of Solitude", report.books().getFirst().title());
        assertEquals("https://cdn.example.com/cover-b.jpg", report.books().getFirst().primaryImageUrl());
        assertNotNull(report.generatedAt());
    }

    @Test
    void getBooksByAuthorIdCardThrowsWhenAuthorDoesNotExist() {
        when(authorRepository.findByIdCard("0000")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportService.getBooksByAuthorIdCard("0000"));
    }

    @Test
    void getBooksByAuthorIdCardPdfReturnsPdfBytes() {
        Author author = mock(Author.class);
        when(author.getId()).thenReturn(4L);
        when(author.getIdCard()).thenReturn("9999");
        when(author.getFullName()).thenReturn("Isabel Allende");

        when(authorRepository.findByIdCard("9999")).thenReturn(Optional.of(author));
        when(bookRepository.findByAuthorIdOrderByTitleAsc(4L)).thenReturn(List.of());

        byte[] pdfBytes = reportService.getBooksByAuthorIdCardPdf("9999");

        assertTrue(pdfBytes.length > 0);
        String signature = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", signature);
    }
}
