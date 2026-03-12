package com.Biblioteca.MunicipalBack.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Biblioteca.MunicipalBack.auth.dto.AuthResponse;
import com.Biblioteca.MunicipalBack.auth.dto.LoginRequest;
import com.Biblioteca.MunicipalBack.auth.dto.LogoutRequest;
import com.Biblioteca.MunicipalBack.auth.dto.RefreshTokenRequest;
import com.Biblioteca.MunicipalBack.auth.model.RefreshToken;
import com.Biblioteca.MunicipalBack.auth.security.JwtService;
import com.Biblioteca.MunicipalBack.shared.exceptions.InvalidCredentialsException;
import com.Biblioteca.MunicipalBack.users.model.User;
import com.Biblioteca.MunicipalBack.users.repository.UserRepository;



@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        refreshTokenService.revokeAllByUser(user);

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken currentRefreshToken = refreshTokenService.verify(request.refreshToken());
        RefreshToken newRefreshToken = refreshTokenService.rotate(currentRefreshToken);

        User user = currentRefreshToken.getUser();

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        return new AuthResponse(
                accessToken,
                newRefreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }

    @Override
    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }
}
