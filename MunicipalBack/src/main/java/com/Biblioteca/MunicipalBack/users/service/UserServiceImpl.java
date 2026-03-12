package com.Biblioteca.MunicipalBack.users.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import com.Biblioteca.MunicipalBack.users.dto.UserRequest;
import com.Biblioteca.MunicipalBack.users.dto.UserResponse;
import com.Biblioteca.MunicipalBack.users.model.User;
import com.Biblioteca.MunicipalBack.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return toResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!existing.getUsername().equals(request.username())
                && userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        existing.setUsername(request.username());
        existing.setPassword(passwordEncoder.encode(request.password()));
        existing.setRole(request.role());

        return toResponse(userRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(existing);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}