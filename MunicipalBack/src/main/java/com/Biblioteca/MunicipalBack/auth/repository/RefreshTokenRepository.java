package com.Biblioteca.MunicipalBack.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Biblioteca.MunicipalBack.auth.model.RefreshToken;
import com.Biblioteca.MunicipalBack.users.model.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUser(User user);
}