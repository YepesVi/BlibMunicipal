package com.Biblioteca.MunicipalBack.users.service;

import java.util.List;

import com.Biblioteca.MunicipalBack.users.dto.UserRequest;
import com.Biblioteca.MunicipalBack.users.dto.UserResponse;

public interface UserService {
    UserResponse create(UserRequest request);
    List<UserResponse> findAll();
    UserResponse findById(Long id);
    UserResponse update(Long id, UserRequest request);
    void delete(Long id);
}
