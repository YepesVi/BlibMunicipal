package com.Biblioteca.MunicipalBack.catalog.categories.service;

import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryTreeResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CreateCategoryRequest;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);

    List<CategoryResponse> findAll();

    List<CategoryResponse> findRoots();

    List<CategoryResponse> findChildren(Long parentId);

    CategoryResponse findById(Long id);

    CategoryTreeResponse findTreeById(Long id);

    CategoryResponse update(Long id, UpdateCategoryRequest request);

    void delete(Long id);

    CategoryResponse findEntityAsResponse(Long id);
}