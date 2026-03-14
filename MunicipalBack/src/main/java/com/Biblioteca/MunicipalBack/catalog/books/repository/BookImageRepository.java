package com.Biblioteca.MunicipalBack.catalog.books.repository;

import com.Biblioteca.MunicipalBack.catalog.books.model.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {

    List<BookImage> findByBookIdOrderBySortOrderAsc(Long bookId);

    Optional<BookImage> findByBookIdAndPrimaryImageTrue(Long bookId);

    boolean existsByBookIdAndMediaAssetId(Long bookId, Long mediaAssetId);
    
    boolean existsByMediaAssetId(Long mediaAssetId);
}