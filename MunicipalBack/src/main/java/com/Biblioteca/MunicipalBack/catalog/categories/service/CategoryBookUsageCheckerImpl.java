package com.Biblioteca.MunicipalBack.catalog.categories.service;

import com.Biblioteca.MunicipalBack.catalog.books.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class CategoryBookUsageCheckerImpl implements CategoryBookUsageChecker {

    private final BookRepository bookRepository;

    @Override
    public boolean hasBooksAssociated(Long categoryId) {
        return bookRepository.existsByCategoryId(categoryId);
    }
}