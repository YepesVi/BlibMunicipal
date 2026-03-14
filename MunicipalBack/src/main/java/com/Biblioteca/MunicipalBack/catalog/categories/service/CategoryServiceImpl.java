package com.Biblioteca.MunicipalBack.catalog.categories.service;

import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryTreeResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CreateCategoryRequest;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.UpdateCategoryRequest;
import com.Biblioteca.MunicipalBack.catalog.categories.model.Category;
import com.Biblioteca.MunicipalBack.catalog.categories.repository.CategoryRepository;
import com.Biblioteca.MunicipalBack.shared.exceptions.ConflictException;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryBookUsageChecker categoryBookUsageChecker;

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        Category parent = resolveParent(request.parentId());

        validateUniqueNameForSameLevel(request.name(), request.parentId(), null);

        Category category = Category.builder()
                .name(request.name().trim())
                .description(normalizeDescription(request.description()))
                .parent(parent)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> findRoots() {
        return categoryRepository.findByParentIsNullOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> findChildren(Long parentId) {
        findEntity(parentId);

        return categoryRepository.findByParentIdOrderByNameAsc(parentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    public CategoryTreeResponse findTreeById(Long id) {
        Category category = findEntity(id);
        return toTreeResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category existing = findEntity(id);
        Category newParent = resolveParent(request.parentId());

        validateNotSelfParent(id, request.parentId());
        validateNoCycle(existing, newParent);
        validateUniqueNameForSameLevel(request.name(), request.parentId(), id);

        existing.setName(request.name().trim());
        existing.setDescription(normalizeDescription(request.description()));
        existing.setParent(newParent);

        return toResponse(categoryRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findEntity(id);

        if (categoryRepository.existsByParentId(id)) {
            throw new ConflictException("Cannot delete category because it has child categories");
        }

        if (categoryBookUsageChecker.hasBooksAssociated(id)) {
            throw new ConflictException("Cannot delete category because it has books associated");
        }

        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse findEntityAsResponse(Long id) {
        return toResponse(findEntity(id));
    }

    private Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return findEntity(parentId);
    }

    private void validateNotSelfParent(Long categoryId, Long parentId) {
        if (parentId != null && parentId.equals(categoryId)) {
            throw new ConflictException("A category cannot be its own parent");
        }
    }

    private void validateNoCycle(Category categoryToMove, Category proposedParent) {
        if (proposedParent == null) {
            return;
        }

        Category current = proposedParent;
        while (current != null) {
            if (current.getId().equals(categoryToMove.getId())) {
                throw new ConflictException("Cannot assign a descendant category as parent");
            }
            current = current.getParent();
        }
    }

    private void validateUniqueNameForSameLevel(String name, Long parentId, Long currentCategoryId) {
        boolean exists;

        if (parentId == null) {
            exists = categoryRepository.existsByNameIgnoreCaseAndParentIsNull(name.trim());
        } else {
            exists = categoryRepository.existsByNameIgnoreCaseAndParentId(name.trim(), parentId);
        }

        if (!exists) {
            return;
        }

        if (currentCategoryId == null) {
            throw new ConflictException("A category with the same name already exists at this level");
        }

        Category current = findEntity(currentCategoryId);
        boolean sameName = current.getName().equalsIgnoreCase(name.trim());
        boolean sameParent = (current.getParent() == null && parentId == null)
                || (current.getParent() != null && current.getParent().getId().equals(parentId));

        if (!(sameName && sameParent)) {
            throw new ConflictException("A category with the same name already exists at this level");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private CategoryTreeResponse toTreeResponse(Category category) {
        List<CategoryTreeResponse> children = category.getChildren()
                .stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toTreeResponse)
                .toList();

        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                children
        );
    }
}