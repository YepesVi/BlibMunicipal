package com.Biblioteca.MunicipalBack.catalog.categories.graphql;

import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.dto.CategoryTreeResponse;
import com.Biblioteca.MunicipalBack.catalog.categories.service.CategoryService;
import com.Biblioteca.MunicipalBack.shared.graphql.GraphQlExceptionResolver;
import com.Biblioteca.MunicipalBack.shared.graphql.GraphQlScalarConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@GraphQlTest(CategoryGraphQlController.class)
@Import({GraphQlScalarConfig.class, GraphQlExceptionResolver.class})
@EnableMethodSecurity
class CategoryGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void categories_returnsList() {
        var now = LocalDateTime.now();
        var category = new CategoryResponse(1L, "Fiction", "Fiction books", null, null, now, now);

        when(categoryService.findAll()).thenReturn(List.of(category));

        graphQlTester.document("""
                        query {
                            categories {
                                id name description
                            }
                        }
                        """)
                .execute()
                .path("categories[0].name").entity(String.class).isEqualTo("Fiction");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void categoryTree_returnsHierarchy() {
        var now = LocalDateTime.now();
        var child = new CategoryTreeResponse(2L, "Sci-Fi", "Science fiction", now, now, List.of());
        var tree = new CategoryTreeResponse(1L, "Fiction", "All fiction", now, now, List.of(child));

        when(categoryService.findTreeById(1L)).thenReturn(tree);

        graphQlTester.document("""
                        query {
                            categoryTree(id: 1) {
                                id name
                                children { id name }
                            }
                        }
                        """)
                .execute()
                .path("categoryTree.name").entity(String.class).isEqualTo("Fiction")
                .path("categoryTree.children[0].name").entity(String.class).isEqualTo("Sci-Fi");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_returnsCreated() {
        var now = LocalDateTime.now();
        var category = new CategoryResponse(1L, "New Cat", null, null, null, now, now);

        when(categoryService.create(org.mockito.ArgumentMatchers.any())).thenReturn(category);

        graphQlTester.document("""
                        mutation {
                            createCategory(input: { name: "New Cat" }) {
                                id name
                            }
                        }
                        """)
                .execute()
                .path("createCategory.name").entity(String.class).isEqualTo("New Cat");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deleteCategory_forbiddenForEmployee() {
        graphQlTester.document("""
                        mutation {
                            deleteCategory(id: 1)
                        }
                        """)
                .execute()
                .errors()
                .expect(error -> error.getMessage() != null && error.getMessage().contains("Access denied"));
    }
}
