package com.Biblioteca.MunicipalBack.reports.service;

import com.Biblioteca.MunicipalBack.catalog.authors.model.Author;
import com.Biblioteca.MunicipalBack.catalog.authors.repository.AuthorRepository;
import com.Biblioteca.MunicipalBack.catalog.books.model.Book;
import com.Biblioteca.MunicipalBack.catalog.books.model.BookImage;
import com.Biblioteca.MunicipalBack.catalog.books.repository.BookRepository;
import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportItemResponse;
import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportResponse;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Override
    public BooksByAuthorReportResponse getBooksByAuthorIdCard(String idCard) {
        Author author = findAuthorByIdCard(idCard);
        List<BooksByAuthorReportItemResponse> items = findBooksByAuthor(author.getId());

        return new BooksByAuthorReportResponse(
                author.getIdCard(),
                author.getFullName(),
                OffsetDateTime.now(),
                items.size(),
                items
        );
    }

    @Override
    public byte[] getBooksByAuthorIdCardPdf(String idCard) {
        BooksByAuthorReportResponse report = getBooksByAuthorIdCard(idCard);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Books Report by Author", titleFont));
            document.add(new Paragraph("Generated at: " + report.generatedAt(), textFont));
            document.add(new Paragraph("Author: " + report.authorName() + " (" + report.authorIdCard() + ")", textFont));
            document.add(new Paragraph("Total books: " + report.totalBooks(), textFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{2.2f, 1.7f, 1.8f, 1.0f, 1.6f, 2.5f});

            addHeaderCell(table, "Title");
            addHeaderCell(table, "ISBN");
            addHeaderCell(table, "Publisher");
            addHeaderCell(table, "Year");
            addHeaderCell(table, "Category");
            addHeaderCell(table, "Primary Image URL");

            if (report.books().isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No books associated with this author"));
                emptyCell.setColspan(6);
                table.addCell(emptyCell);
            } else {
                for (BooksByAuthorReportItemResponse item : report.books()) {
                    table.addCell(safe(item.title()));
                    table.addCell(safe(item.isbn()));
                    table.addCell(safe(item.publisher()));
                    table.addCell(item.publicationYear() == null ? "-" : String.valueOf(item.publicationYear()));
                    table.addCell(safe(item.categoryName()));
                    table.addCell(safe(item.primaryImageUrl()));
                }
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Failed to generate report PDF", exception);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Author findAuthorByIdCard(String idCard) {
        String normalizedIdCard = idCard == null ? "" : idCard.trim();
        return authorRepository.findByIdCard(normalizedIdCard)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id card: " + idCard));
    }

    private List<BooksByAuthorReportItemResponse> findBooksByAuthor(Long authorId) {
        List<Book> books = bookRepository.findByAuthorIdOrderByTitleAsc(authorId);
        return books.stream().map(this::toReportItem).toList();
    }

    private BooksByAuthorReportItemResponse toReportItem(Book book) {
        String primaryImageUrl = book.getImages().stream()
                .filter(BookImage::isPrimaryImage)
                .min(Comparator.comparing(BookImage::getSortOrder))
                .map(image -> image.getMediaAsset().getSecureUrl())
                .orElse(null);

        return new BooksByAuthorReportItemResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getPublisher(),
                book.getPublicationYear(),
                book.getCategory().getName(),
                primaryImageUrl
        );
    }

    private void addHeaderCell(PdfPTable table, String value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        table.addCell(cell);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
