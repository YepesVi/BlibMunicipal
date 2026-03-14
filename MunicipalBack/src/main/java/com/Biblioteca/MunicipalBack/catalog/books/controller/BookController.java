package com.Biblioteca.MunicipalBack.catalog.books.controller;

import com.Biblioteca.MunicipalBack.catalog.books.dto.*;
import com.Biblioteca.MunicipalBack.catalog.books.service.BookService;
import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody CreateBookRequest request) {
        return bookService.create(request);
    }

    @GetMapping
    public PageResponse<BookSummaryResponse> findAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return bookService.findAll(new BookFilterRequest(
                title, authorId, categoryId, publicationYear, page, size, sortBy, sortDir
        ));
    }

    @GetMapping("/{id}")
    public BookResponse findById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @GetMapping("/isbn/{isbn}")
    public BookResponse findByIsbn(@PathVariable String isbn) {
        return bookService.findByIsbn(isbn);
    }

    @PutMapping("/{id}")
    public BookResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request
    ) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }

    @PostMapping("/{bookId}/images")
    public BookResponse attachImages(
            @PathVariable Long bookId,
            @Valid @RequestBody AttachBookImagesRequest request
    ) {
        return bookService.attachImages(bookId, request);
    }

    @PatchMapping("/{bookId}/images/{bookImageId}/primary")
    public BookResponse setPrimaryImage(
            @PathVariable Long bookId,
            @PathVariable Long bookImageId
    ) {
        return bookService.setPrimaryImage(bookId, bookImageId);
    }

    @PatchMapping("/{bookId}/images/reorder")
    public BookResponse reorderImages(
            @PathVariable Long bookId,
            @Valid @RequestBody ReorderBookImagesRequest request
    ) {
        return bookService.reorderImages(bookId, request);
    }

    @DeleteMapping("/{bookId}/images/{bookImageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeImage(
            @PathVariable Long bookId,
            @PathVariable Long bookImageId
    ) {
        bookService.removeImage(bookId, bookImageId);
    }
}