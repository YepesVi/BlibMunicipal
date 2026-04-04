package com.Biblioteca.MunicipalBack.catalog.categories.graphql;

import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryTreeResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CreateCategoryRequest;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.UpdateCategoryRequest;
import com.Biblioteca.MunicipalBack.catalog.categories.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryGraphQlController {

    private final CategoryService categoryService;

    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public List<CategoryResponse> categories() {
        return categoryService.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public List<CategoryResponse> categoryRoots() {
        return categoryService.findRoots();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public List<CategoryResponse> categoryChildren(@Argument Long parentId) {
        return categoryService.findChildren(parentId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public CategoryResponse category(@Argument Long id) {
        return categoryService.findById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public CategoryTreeResponse categoryTree(@Argument Long id) {
        return categoryService.findTreeById(id);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(@Argument CreateCategoryInput input) {
        return categoryService.create(new CreateCategoryRequest(
                input.name(), input.description(), input.parentId()));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(@Argument Long id, @Argument UpdateCategoryInput input) {
        return categoryService.update(id, new UpdateCategoryRequest(
                input.name(), input.description(), input.parentId()));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteCategory(@Argument Long id) {
        categoryService.delete(id);
        return true;
    }
}
