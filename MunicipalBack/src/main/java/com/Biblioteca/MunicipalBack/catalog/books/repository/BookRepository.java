 package com.Biblioteca.MunicipalBack.catalog.books.repository;

import com.Biblioteca.MunicipalBack.catalog.books.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByAuthorId(Long authorId);

    boolean existsByCategoryId(Long categoryId);
}