package com.Biblioteca.MunicipalBack.catalog.books.service;

import com.Biblioteca.MunicipalBack.catalog.authors.repository.AuthorRepository;
import com.Biblioteca.MunicipalBack.catalog.books.repository.BookImageRepository;
import com.Biblioteca.MunicipalBack.catalog.books.repository.BookRepository;
import com.Biblioteca.MunicipalBack.catalog.categories.model.Category;
import com.Biblioteca.MunicipalBack.catalog.categories.repository.CategoryRepository;
import com.Biblioteca.MunicipalBack.media.repository.MediaAssetRepository;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookImageRepository bookImageRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MediaAssetRepository mediaAssetRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void collectCategoryAndDescendantIdsIncludesAllChildrenRecursively() {
        Category root = new Category();
        root.setId(1L);

        Category child = new Category();
        child.setId(2L);
        child.setParent(root);

        Category grandChild = new Category();
        grandChild.setId(3L);
        grandChild.setParent(child);

        Category otherRoot = new Category();
        otherRoot.setId(10L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
        when(categoryRepository.findAll()).thenReturn(List.of(root, child, grandChild, otherRoot));

        @SuppressWarnings("unchecked")
        Set<Long> ids = (Set<Long>) ReflectionTestUtils.invokeMethod(
                bookService,
                "collectCategoryAndDescendantIds",
                1L
        );

        assertEquals(Set.of(1L, 2L, 3L), ids);
    }

    @Test
    void collectCategoryAndDescendantIdsThrowsWhenRootDoesNotExist() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ReflectionTestUtils.invokeMethod(bookService, "collectCategoryAndDescendantIds", 999L)
        );
    }
}
