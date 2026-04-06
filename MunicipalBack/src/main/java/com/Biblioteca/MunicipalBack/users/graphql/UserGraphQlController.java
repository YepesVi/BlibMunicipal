package com.Biblioteca.MunicipalBack.users.graphql;

import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;
import com.Biblioteca.MunicipalBack.shared.enums.UserRole;
import com.Biblioteca.MunicipalBack.users.dto.CreateUserRequest;
import com.Biblioteca.MunicipalBack.users.dto.UpdateUserRequest;
import com.Biblioteca.MunicipalBack.users.dto.UserResponse;
import com.Biblioteca.MunicipalBack.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserGraphQlController {

    private final UserService userService;

    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> users(@Argument UserFilterInput filter) {
        UserFilterInput f = filter != null ? filter : new UserFilterInput(null, null, 0, 10, "username", "asc");
        return userService.findAll(f.username(), toRole(f.role()), f.page(), f.size(), f.sortBy(), f.sortDir());
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse user(@Argument Long id) {
        return userService.findById(id);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(@Argument CreateUserInput input) {
        return userService.create(new CreateUserRequest(
                input.username(), input.password(), toRole(input.role())));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@Argument Long id, @Argument UpdateUserInput input) {
        return userService.update(id, new UpdateUserRequest(
                input.username(), input.password(), toRole(input.role())));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteUser(@Argument Long id) {
        userService.delete(id);
        return true;
    }

    // ── Converter ─────────────────────────────────────────────────────────────

    private UserRole toRole(String roleStr) {
        return roleStr != null ? UserRole.valueOf(roleStr) : null;
    }
}
