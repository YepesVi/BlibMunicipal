package com.Biblioteca.MunicipalBack.catalog.authors.service;

import com.Biblioteca.MunicipalBack.catalog.authors.dto.AuthorResponse;
import com.Biblioteca.MunicipalBack.catalog.authors.dto.CreateAuthorRequest;
import com.Biblioteca.MunicipalBack.catalog.authors.dto.UpdateAuthorRequest;

import java.util.List;

public interface AuthorService {

    AuthorResponse create(CreateAuthorRequest request);

    List<AuthorResponse> findAll();

    List<AuthorResponse> searchByFullName(String fullName);

    AuthorResponse findById(Long id);

    AuthorResponse findByIdCard(String idCard);

    AuthorResponse update(Long id, UpdateAuthorRequest request);

    void delete(Long id);
}