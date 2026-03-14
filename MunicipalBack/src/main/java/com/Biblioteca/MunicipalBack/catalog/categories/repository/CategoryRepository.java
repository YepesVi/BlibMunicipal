package com.Biblioteca.MunicipalBack.catalog.categories.repository;

import com.Biblioteca.MunicipalBack.catalog.categories.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNullOrderByNameAsc();

    List<Category> findByParentIdOrderByNameAsc(Long parentId);

    boolean existsByParentId(Long parentId);

    boolean existsByNameIgnoreCaseAndParentId(String name, Long parentId);

    boolean existsByNameIgnoreCaseAndParentIsNull(String name);
}