package com.Biblioteca.MunicipalBack.catalog.books.graphql;

import com.Biblioteca.MunicipalBack.catalog.books.dto.BookImageResponse;
import com.Biblioteca.MunicipalBack.catalog.books.dto.BookResponse;
import com.Biblioteca.MunicipalBack.catalog.books.dto.BookSummaryResponse;
import com.Biblioteca.MunicipalBack.catalog.books.service.BookService;
import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;
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

@GraphQlTest(BookGraphQlController.class)
@Import({GraphQlScalarConfig.class, GraphQlExceptionResolver.class})
@EnableMethodSecurity
class BookGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private BookService bookService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void books_returnsPage() {
        var now = LocalDateTime.now();
        var summary = new BookSummaryResponse(1L, "978-0-123", "Test Book", "Publisher",
                2024, 1L, "Author Name", 1L, "Category", "http://img.url", now, now);
        var page = new PageResponse<>(List.of(summary), 0, 10, 1L, 1, true, true, "title", "asc");

        when(bookService.findAll(any())).thenReturn(page);

        graphQlTester.document("""
                        query {
                            books {
                                content { id isbn title authorName categoryName }
                                totalElements totalPages page size
                            }
                        }
                        """)
                .execute()
                .path("books.content[0].title").entity(String.class).isEqualTo("Test Book")
                .path("books.totalElements").entity(Long.class).isEqualTo(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void book_returnsDetail() {
        var now = LocalDateTime.now();
        var book = new BookResponse(1L, "978-0-123", "Test Book", "Publisher", 2024,
                "Description", 1L, "Author", 1L, "Category", "http://img.url",
                List.of(new BookImageResponse(1L, 10L, "http://img.url", true, 0, "alt")), now, now);

        when(bookService.findById(1L)).thenReturn(book);

        graphQlTester.document("""
                        query {
                            book(id: 1) {
                                id isbn title description
                                authorName categoryName
                                images { id secureUrl primaryImage }
                            }
                        }
                        """)
                .execute()
                .path("book.title").entity(String.class).isEqualTo("Test Book")
                .path("book.images[0].primaryImage").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBook_returnsCreatedBook() {
        var now = LocalDateTime.now();
        var book = new BookResponse(1L, "978-0-123", "New Book", "Pub", 2024,
                null, 1L, "Author", 1L, "Cat", null, List.of(), now, now);

        when(bookService.create(any())).thenReturn(book);

        graphQlTester.document("""
                        mutation {
                            createBook(input: {
                                isbn: "978-0-123"
                                title: "New Book"
                                publisher: "Pub"
                                publicationYear: 2024
                                authorId: 1
                                categoryId: 1
                            }) {
                                id isbn title
                            }
                        }
                        """)
                .execute()
                .path("createBook.title").entity(String.class).isEqualTo("New Book");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_returnsTrue() {
        graphQlTester.document("""
                        mutation {
                            deleteBook(id: 1)
                        }
                        """)
                .execute()
                .path("deleteBook").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createBook_forbiddenForEmployee() {
        graphQlTester.document("""
                        mutation {
                            createBook(input: {
                                isbn: "978-0-123"
                                title: "New Book"
                                publisher: "Pub"
                                publicationYear: 2024
                                authorId: 1
                                categoryId: 1
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
