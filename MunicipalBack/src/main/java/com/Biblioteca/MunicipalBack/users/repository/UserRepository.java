package com.Biblioteca.MunicipalBack.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Biblioteca.MunicipalBack.users.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
