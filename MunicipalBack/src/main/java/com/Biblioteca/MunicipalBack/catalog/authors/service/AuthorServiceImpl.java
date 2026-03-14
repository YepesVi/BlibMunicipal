package com.Biblioteca.MunicipalBack.catalog.authors.service;

import com.Biblioteca.MunicipalBack.catalog.authors.dto.AuthorResponse;
import com.Biblioteca.MunicipalBack.catalog.authors.dto.CreateAuthorRequest;
import com.Biblioteca.MunicipalBack.catalog.authors.dto.UpdateAuthorRequest;
import com.Biblioteca.MunicipalBack.catalog.authors.model.Author;
import com.Biblioteca.MunicipalBack.catalog.authors.repository.AuthorRepository;
import com.Biblioteca.MunicipalBack.shared.exceptions.ConflictException;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorBookUsageChecker authorBookUsageChecker;

    @Override
    @Transactional
    public AuthorResponse create(CreateAuthorRequest request) {
        validateUniqueIdCard(request.idCard(), null);

        Author author = Author.builder()
                .idCard(request.idCard().trim())
                .fullName(request.fullName().trim())
                .nationality(request.nationality().trim())
                .biography(normalizeBiography(request.biography()))
                .build();

        return toResponse(authorRepository.save(author));
    }

    @Override
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Author::getFullName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AuthorResponse> searchByFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return findAll();
        }

        return authorRepository.findByFullNameContainingIgnoreCaseOrderByFullNameAsc(fullName.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AuthorResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    public AuthorResponse findByIdCard(String idCard) {
        return authorRepository.findByIdCard(idCard.trim())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id card: " + idCard));
    }

    @Override
    @Transactional
    public AuthorResponse update(Long id, UpdateAuthorRequest request) {
        Author existing = findEntity(id);

        validateUniqueIdCard(request.idCard(), id);

        existing.setIdCard(request.idCard().trim());
        existing.setFullName(request.fullName().trim());
        existing.setNationality(request.nationality().trim());
        existing.setBiography(normalizeBiography(request.biography()));

        return toResponse(authorRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Author author = findEntity(id);

        if (authorBookUsageChecker.hasBooksAssociated(id)) {
            throw new ConflictException("Cannot delete author because it has books associated");
        }

        authorRepository.delete(author);
    }

    private Author findEntity(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }

    private void validateUniqueIdCard(String idCard, Long currentAuthorId) {
        String normalizedIdCard = idCard.trim();

        if (!authorRepository.existsByIdCard(normalizedIdCard)) {
            return;
        }

        if (currentAuthorId == null) {
            throw new ConflictException("Author id card already exists");
        }

        Author current = findEntity(currentAuthorId);
        if (!current.getIdCard().equalsIgnoreCase(normalizedIdCard)) {
            throw new ConflictException("Author id card already exists");
        }
    }

    private String normalizeBiography(String biography) {
        if (biography == null || biography.isBlank()) {
            return null;
        }
        return biography.trim();
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getIdCard(),
                author.getFullName(),
                author.getNationality(),
                author.getBiography(),
                author.getCreatedAt(),
                author.getUpdatedAt()
        );
    }
}