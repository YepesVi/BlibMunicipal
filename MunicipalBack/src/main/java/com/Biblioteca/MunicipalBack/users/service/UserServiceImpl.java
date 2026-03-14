package com.Biblioteca.MunicipalBack.users.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;
import com.Biblioteca.MunicipalBack.shared.enums.UserRole;
import com.Biblioteca.MunicipalBack.shared.exceptions.ConflictException;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import com.Biblioteca.MunicipalBack.users.dto.CreateUserRequest;
import com.Biblioteca.MunicipalBack.users.dto.UpdateUserRequest;
import com.Biblioteca.MunicipalBack.users.dto.UserResponse;
import com.Biblioteca.MunicipalBack.users.model.User;
import com.Biblioteca.MunicipalBack.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }

        User user = User.builder()
                .username(request.username().trim())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return toResponse(userRepository.save(user));
    }

    @Override
    public PageResponse<UserResponse> findAll(
            String username,
            UserRole role,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;

        boolean hasUsername = username != null && !username.isBlank();
        boolean hasRole = role != null;

        if (hasUsername && hasRole) {
            userPage = userRepository.findByUsernameContainingIgnoreCaseAndRole(username.trim(), role, pageable);
        } else if (hasUsername) {
            userPage = userRepository.findByUsernameContainingIgnoreCase(username.trim(), pageable);
        } else if (hasRole) {
            userPage = userRepository.findByRole(role, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return new PageResponse<>(
                userPage.getContent().stream().map(this::toResponse).toList(),
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast(),
                sortBy,
                sortDir
        );
    }

    @Override
    public UserResponse findById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User existing = findEntityById(id);

        if (!existing.getUsername().equalsIgnoreCase(request.username())
                && userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }

        existing.setUsername(request.username().trim());
        existing.setRole(request.role());

        if (request.password() != null && !request.password().isBlank()) {
            existing.setPassword(passwordEncoder.encode(request.password()));
        }

        return toResponse(userRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User existing = findEntityById(id);
        userRepository.delete(existing);
    }

    private User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}