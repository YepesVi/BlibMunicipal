package com.Biblioteca.MunicipalBack.catalog.authors.graphql;

import com.Biblioteca.MunicipalBack.catalog.authors.dto.AuthorResponse;
import com.Biblioteca.MunicipalBack.catalog.authors.service.AuthorService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(AuthorGraphQlController.class)
@Import({GraphQlScalarConfig.class, GraphQlExceptionResolver.class})
@EnableMethodSecurity
class AuthorGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private AuthorService authorService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void authors_returnsList() {
        var now = LocalDateTime.now();
        var author = new AuthorResponse(1L, "ID001", "Gabriel García Márquez", "Colombian",
                "Nobel laureate", now, now);

        when(authorService.findAll()).thenReturn(List.of(author));

        graphQlTester.document("""
                        query {
                            authors {
                                id idCard fullName nationality
                            }
                        }
                        """)
                .execute()
                .path("authors[0].fullName").entity(String.class).isEqualTo("Gabriel García Márquez");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void author_returnsById() {
        var now = LocalDateTime.now();
        var author = new AuthorResponse(1L, "ID001", "Author Name", "Colombian", null, now, now);

        when(authorService.findById(1L)).thenReturn(author);

        graphQlTester.document("""
                        query {
                            author(id: 1) {
                                id fullName nationality biography
                            }
                        }
                        """)
                .execute()
                .path("author.fullName").entity(String.class).isEqualTo("Author Name");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAuthor_returnsCreated() {
        var now = LocalDateTime.now();
        var author = new AuthorResponse(1L, "ID001", "New Author", "Mexican", null, now, now);

        when(authorService.create(any())).thenReturn(author);

        graphQlTester.document("""
                        mutation {
                            createAuthor(input: {
                                idCard: "ID001"
                                fullName: "New Author"
                                nationality: "Mexican"
                            }) {
                                id fullName nationality
                            }
                        }
                        """)
                .execute()
                .path("createAuthor.fullName").entity(String.class).isEqualTo("New Author");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createAuthor_forbiddenForEmployee() {
        graphQlTester.document("""
                        mutation {
                            createAuthor(input: {
                                idCard: "ID001"
                                fullName: "New Author"
                                nationality: "Mexican"
                            }) {
                                id
                            }
                        }
                        """)
                .execute()
                .errors()
                .expect(error -> error.getMessage() != null && error.getMessage().contains("Access denied"));
    }
}
