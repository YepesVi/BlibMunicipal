package com.Biblioteca.MunicipalBack.catalog.authors.repository;

import com.Biblioteca.MunicipalBack.catalog.authors.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByIdCard(String idCard);

    boolean existsByIdCard(String idCard);

    List<Author> findByFullNameContainingIgnoreCaseOrderByFullNameAsc(String fullName);
}