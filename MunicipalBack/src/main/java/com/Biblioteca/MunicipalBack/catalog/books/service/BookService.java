package com.Biblioteca.MunicipalBack.catalog.books.service;

import com.Biblioteca.MunicipalBack.catalog.books.dto.*;
import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;

public interface BookService {

    BookResponse create(CreateBookRequest request);

    PageResponse<BookSummaryResponse> findAll(BookFilterRequest filter);

    BookResponse findById(Long id);

    BookResponse findByIsbn(String isbn);

    BookResponse update(Long id, UpdateBookRequest request);

    void delete(Long id);

    BookResponse attachImages(Long bookId, AttachBookImagesRequest request);

    BookResponse setPrimaryImage(Long bookId, Long bookImageId);

    BookResponse reorderImages(Long bookId, ReorderBookImagesRequest request);

    void removeImage(Long bookId, Long bookImageId);
}
