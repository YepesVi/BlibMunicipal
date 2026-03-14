package com.Biblioteca.MunicipalBack.catalog.authors.controller;

import com.Biblioteca.MunicipalBack.catalog.authors.dto.AuthorResponse;
import com.Biblioteca.MunicipalBack.catalog.authors.dto.CreateAuthorRequest;
import com.Biblioteca.MunicipalBack.catalog.authors.dto.UpdateAuthorRequest;
import com.Biblioteca.MunicipalBack.catalog.authors.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse create(@Valid @RequestBody CreateAuthorRequest request) {
        return authorService.create(request);
    }

    @GetMapping
    public List<AuthorResponse> findAll(
            @RequestParam(required = false) String fullName
    ) {
        return authorService.searchByFullName(fullName);
    }

    @GetMapping("/{id}")
    public AuthorResponse findById(@PathVariable Long id) {
        return authorService.findById(id);
    }

    @GetMapping("/id-card/{idCard}")
    public AuthorResponse findByIdCard(@PathVariable String idCard) {
        return authorService.findByIdCard(idCard);
    }

    @PutMapping("/{id}")
    public AuthorResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAuthorRequest request
    ) {
        return authorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        authorService.delete(id);
    }
}