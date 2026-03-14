package com.Biblioteca.MunicipalBack.auth.service;

import com.Biblioteca.MunicipalBack.auth.model.RefreshToken;
import com.Biblioteca.MunicipalBack.users.model.User;

public interface RefreshTokenService {
    RefreshToken create(User user);

    RefreshToken verify(String token);

    RefreshToken rotate(RefreshToken currentToken);

    void revoke(String token);

    void revokeAllByUser(User user);

}
