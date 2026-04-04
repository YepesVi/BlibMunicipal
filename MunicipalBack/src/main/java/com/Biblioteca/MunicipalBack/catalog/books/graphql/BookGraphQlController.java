package com.Biblioteca.MunicipalBack.catalog.books.graphql;

import com.Biblioteca.MunicipalBack.catalog.books.dto.AttachBookImagesRequest;
import com.Biblioteca.MunicipalBack.catalog.books.dto.AttachBookImagesRequest.BookImageAttachItem;
import com.Biblioteca.MunicipalBack.catalog.books.dto.BookResponse;
import com.Biblioteca.MunicipalBack.catalog.books.dto.BookSummaryResponse;
import com.Biblioteca.MunicipalBack.catalog.books.dto.BookFilterRequest;
import com.Biblioteca.MunicipalBack.catalog.books.dto.CreateBookRequest;
import com.Biblioteca.MunicipalBack.catalog.books.dto.ReorderBookImageItemRequest;
import com.Biblioteca.MunicipalBack.catalog.books.dto.ReorderBookImagesRequest;
import com.Biblioteca.MunicipalBack.catalog.books.dto.UpdateBookRequest;
import com.Biblioteca.MunicipalBack.catalog.books.service.BookService;
import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookGraphQlController {

    private final BookService bookService;

    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public PageResponse<BookSummaryResponse> books(@Argument BookFilterInput filter) {
        return bookService.findAll(toFilterRequest(filter));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public BookResponse book(@Argument Long id) {
        return bookService.findById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public BookResponse bookByIsbn(@Argument String isbn) {
        return bookService.findByIsbn(isbn);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse createBook(@Argument CreateBookInput input) {
        return bookService.create(toCreateRequest(input));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse updateBook(@Argument Long id, @Argument UpdateBookInput input) {
        return bookService.update(id, toUpdateRequest(input));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteBook(@Argument Long id) {
        bookService.delete(id);
        return true;
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse attachBookImages(@Argument Long bookId, @Argument AttachImagesInput input) {
        return bookService.attachImages(bookId, toAttachRequest(input));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse setPrimaryBookImage(@Argument Long bookId, @Argument Long bookImageId) {
        return bookService.setPrimaryImage(bookId, bookImageId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse reorderBookImages(@Argument Long bookId, @Argument ReorderImagesInput input) {
        return bookService.reorderImages(bookId, toReorderRequest(input));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean removeBookImage(@Argument Long bookId, @Argument Long bookImageId) {
        bookService.removeImage(bookId, bookImageId);
        return true;
    }

    // ── Converters ────────────────────────────────────────────────────────────

    private BookFilterRequest toFilterRequest(BookFilterInput f) {
        if (f == null) {
            return new BookFilterRequest(null, null, null, null, 0, 10, "title", "asc");
        }
        return new BookFilterRequest(
                f.title(), f.authorId(), f.categoryId(), f.publicationYear(),
                f.page(), f.size(), f.sortBy(), f.sortDir());
    }

    private CreateBookRequest toCreateRequest(CreateBookInput i) {
        return new CreateBookRequest(
                i.isbn(), i.title(), i.publisher(), i.publicationYear(),
                i.description(), i.authorId(), i.categoryId());
    }

    private UpdateBookRequest toUpdateRequest(UpdateBookInput i) {
        return new UpdateBookRequest(
                i.isbn(), i.title(), i.publisher(), i.publicationYear(),
                i.description(), i.authorId(), i.categoryId());
    }

    private AttachBookImagesRequest toAttachRequest(AttachImagesInput i) {
        List<BookImageAttachItem> items = i.images().stream()
                .map(img -> new BookImageAttachItem(img.mediaAssetId(), img.primaryImage(), img.altText()))
                .toList();
        return new AttachBookImagesRequest(items);
    }

    private ReorderBookImagesRequest toReorderRequest(ReorderImagesInput i) {
        List<ReorderBookImageItemRequest> items = i.items().stream()
                .map(r -> new ReorderBookImageItemRequest(r.bookImageId(), r.sortOrder()))
                .toList();
        return new ReorderBookImagesRequest(items);
    }
}
