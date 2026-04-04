package com.Biblioteca.MunicipalBack.users.graphql;

import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;
import com.Biblioteca.MunicipalBack.shared.enums.UserRole;
import com.Biblioteca.MunicipalBack.shared.graphql.GraphQlExceptionResolver;
import com.Biblioteca.MunicipalBack.shared.graphql.GraphQlScalarConfig;
import com.Biblioteca.MunicipalBack.users.dto.UserResponse;
import com.Biblioteca.MunicipalBack.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@GraphQlTest(UserGraphQlController.class)
@Import({GraphQlScalarConfig.class, GraphQlExceptionResolver.class})
@EnableMethodSecurity
class UserGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void users_returnsPage() {
        var user = new UserResponse(1L, "admin_user", UserRole.ADMIN);
        var page = new PageResponse<>(List.of(user), 0, 10, 1L, 1, true, true, "username", "asc");

        when(userService.findAll(any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        graphQlTester.document("""
                        query {
                            users {
                                content { id username role }
                                totalElements
                            }
                        }
                        """)
                .execute()
                .path("users.content[0].username").entity(String.class).isEqualTo("admin_user")
                .path("users.content[0].role").entity(String.class).isEqualTo("ADMIN");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void user_returnsById() {
        var user = new UserResponse(1L, "test_user", UserRole.EMPLOYEE);

        when(userService.findById(1L)).thenReturn(user);

        graphQlTester.document("""
                        query {
                            user(id: 1) {
                                id username role
                            }
                        }
                        """)
                .execute()
                .path("user.username").entity(String.class).isEqualTo("test_user");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_returnsCreated() {
        var user = new UserResponse(1L, "new_user", UserRole.EMPLOYEE);

        when(userService.create(any())).thenReturn(user);

        graphQlTester.document("""
                        mutation {
                            createUser(input: {
                                username: "new_user"
                                password: "password123"
                                role: EMPLOYEE
                            }) {
                                id username role
                            }
                        }
                        """)
                .execute()
                .path("createUser.username").entity(String.class).isEqualTo("new_user");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void users_forbiddenForEmployee() {
        graphQlTester.document("""
                        query {
                            users {
                                content { id }
                            }
                        }
                        """)
                .execute()
                .errors()
                .expect(error -> error.getMessage() != null && error.getMessage().contains("Access denied"));
    }
}
