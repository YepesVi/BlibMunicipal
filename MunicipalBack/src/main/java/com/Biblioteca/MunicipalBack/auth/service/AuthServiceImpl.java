package com.Biblioteca.MunicipalBack.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.Biblioteca.MunicipalBack.auth.dto.JwtResponse;
import com.Biblioteca.MunicipalBack.auth.dto.LoginRequest;
import com.Biblioteca.MunicipalBack.auth.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    @Override
    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(userDetails);

        return new JwtResponse(token);
    }
}
