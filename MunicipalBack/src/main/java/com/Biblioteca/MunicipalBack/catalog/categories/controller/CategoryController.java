package com.Biblioteca.MunicipalBack.catalog.categories.controller;

import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryTreeResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CreateCategoryRequest;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.UpdateCategoryRequest;
import com.Biblioteca.MunicipalBack.catalog.categories.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(request);
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/roots")
    public List<CategoryResponse> findRoots() {
        return categoryService.findRoots();
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @GetMapping("/{id}/children")
    public List<CategoryResponse> findChildren(@PathVariable Long id) {
        return categoryService.findChildren(id);
    }

    @GetMapping("/{id}/tree")
    public CategoryTreeResponse findTreeById(@PathVariable Long id) {
        return categoryService.findTreeById(id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}