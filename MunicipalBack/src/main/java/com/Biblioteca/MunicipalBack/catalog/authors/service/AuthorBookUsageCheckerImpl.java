package com.Biblioteca.MunicipalBack.catalog.authors.service;

import com.Biblioteca.MunicipalBack.catalog.books.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class AuthorBookUsageCheckerImpl implements AuthorBookUsageChecker {

    private final BookRepository bookRepository;

    @Override
    public boolean hasBooksAssociated(Long authorId) {
        return bookRepository.existsByAuthorId(authorId);
    }
}