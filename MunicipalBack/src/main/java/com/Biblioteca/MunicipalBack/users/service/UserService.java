package com.Biblioteca.MunicipalBack.users.service;

import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;
import com.Biblioteca.MunicipalBack.shared.enums.UserRole;
import com.Biblioteca.MunicipalBack.users.dto.CreateUserRequest;
import com.Biblioteca.MunicipalBack.users.dto.UpdateUserRequest;
import com.Biblioteca.MunicipalBack.users.dto.UserResponse;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    PageResponse<UserResponse> findAll(
            String username,
            UserRole role,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    UserResponse findById(Long id);

    UserResponse update(Long id, UpdateUserRequest request);

    void delete(Long id);
}
