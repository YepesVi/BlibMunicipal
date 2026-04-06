package com.Biblioteca.MunicipalBack.catalog.authors.graphql;

import com.Biblioteca.MunicipalBack.catalog.authors.dto.AuthorResponse;
import com.Biblioteca.MunicipalBack.catalog.authors.dto.CreateAuthorRequest;
import com.Biblioteca.MunicipalBack.catalog.authors.dto.UpdateAuthorRequest;
import com.Biblioteca.MunicipalBack.catalog.authors.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthorGraphQlController {

    private final AuthorService authorService;

    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public List<AuthorResponse> authors(@Argument String fullName) {
        if (fullName != null && !fullName.isBlank()) {
            return authorService.searchByFullName(fullName);
        }
        return authorService.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public AuthorResponse author(@Argument Long id) {
        return authorService.findById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public AuthorResponse authorByIdCard(@Argument String idCard) {
        return authorService.findByIdCard(idCard);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AuthorResponse createAuthor(@Argument CreateAuthorInput input) {
        return authorService.create(new CreateAuthorRequest(
                input.idCard(), input.fullName(), input.nationality(), input.biography()));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AuthorResponse updateAuthor(@Argument Long id, @Argument UpdateAuthorInput input) {
        return authorService.update(id, new UpdateAuthorRequest(
                input.idCard(), input.fullName(), input.nationality(), input.biography()));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteAuthor(@Argument Long id) {
        authorService.delete(id);
        return true;
    }
}
