package com.Biblioteca.MunicipalBack.catalog.books.service;

import com.Biblioteca.MunicipalBack.catalog.authors.model.Author;
import com.Biblioteca.MunicipalBack.catalog.authors.repository.AuthorRepository;
import com.Biblioteca.MunicipalBack.catalog.books.dto.*;
import com.Biblioteca.MunicipalBack.catalog.books.model.Book;
import com.Biblioteca.MunicipalBack.catalog.books.model.BookImage;
import com.Biblioteca.MunicipalBack.catalog.books.repository.BookImageRepository;
import com.Biblioteca.MunicipalBack.catalog.books.repository.BookRepository;
import com.Biblioteca.MunicipalBack.catalog.categories.model.Category;
import com.Biblioteca.MunicipalBack.catalog.categories.repository.CategoryRepository;
import com.Biblioteca.MunicipalBack.media.model.MediaAsset;
import com.Biblioteca.MunicipalBack.media.repository.MediaAssetRepository;
import com.Biblioteca.MunicipalBack.shared.dto.PageResponse;
import com.Biblioteca.MunicipalBack.shared.exceptions.ConflictException;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookImageRepository bookImageRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final MediaAssetRepository mediaAssetRepository;

    @Override
    @Transactional
    public BookResponse create(CreateBookRequest request) {
        validateUniqueIsbn(request.isbn(), null);

        Author author = findAuthor(request.authorId());
        Category category = findCategory(request.categoryId());

        Book book = Book.builder()
                .isbn(request.isbn().trim())
                .title(request.title().trim())
                .publisher(request.publisher().trim())
                .publicationYear(request.publicationYear())
                .description(normalizeDescription(request.description()))
                .author(author)
                .category(category)
                .build();

        return toResponse(bookRepository.save(book));
    }

    @Override
    public PageResponse<BookSummaryResponse> findAll(BookFilterRequest filter) {
        Sort sort = "desc".equalsIgnoreCase(filter.sortDir())
                ? Sort.by(filter.sortBy()).descending()
                : Sort.by(filter.sortBy()).ascending();

        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

        Specification<Book> specification = buildSpecification(filter);

        Page<Book> page = bookRepository.findAll(specification, pageable);

        return new PageResponse<>(
                page.getContent().stream().map(this::toSummaryResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                filter.sortBy(),
                filter.sortDir()
        );
    }

    @Override
    public BookResponse findById(Long id) {
        return toResponse(findBook(id));
    }

    @Override
    public BookResponse findByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with isbn: " + isbn));
        return toResponse(book);
    }

    @Override
    @Transactional
    public BookResponse update(Long id, UpdateBookRequest request) {
        Book existing = findBook(id);
        validateUniqueIsbn(request.isbn(), id);

        existing.setIsbn(request.isbn().trim());
        existing.setTitle(request.title().trim());
        existing.setPublisher(request.publisher().trim());
        existing.setPublicationYear(request.publicationYear());
        existing.setDescription(normalizeDescription(request.description()));
        existing.setAuthor(findAuthor(request.authorId()));
        existing.setCategory(findCategory(request.categoryId()));

        return toResponse(bookRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = findBook(id);
        bookRepository.delete(book);
    }

    @Override
    @Transactional
    public BookResponse attachImages(Long bookId, AttachBookImagesRequest request) {
        Book book = findBook(bookId);

        boolean currentHasPrimary = book.getImages().stream().anyMatch(BookImage::isPrimaryImage);
        boolean requestWantsPrimary = request.images().stream().anyMatch(i -> Boolean.TRUE.equals(i.primaryImage()));

        if (currentHasPrimary && requestWantsPrimary) {
            throw new ConflictException("Book already has a primary image");
        }

        int nextSortOrder = book.getImages().stream()
                .map(BookImage::getSortOrder)
                .max(Integer::compareTo)
                .orElse(0);

        for (AttachBookImagesRequest.BookImageAttachItem item : request.images()) {
            MediaAsset mediaAsset = findMediaAsset(item.mediaAssetId());

            if (bookImageRepository.existsByBookIdAndMediaAssetId(bookId, mediaAsset.getId())) {
                throw new ConflictException("One of the provided media assets is already attached to the book");
            }

            boolean primary = Boolean.TRUE.equals(item.primaryImage());

            if (!currentHasPrimary && !requestWantsPrimary && book.getImages().isEmpty()) {
                primary = true;
                currentHasPrimary = true;
            } else if (primary) {
                currentHasPrimary = true;
            }

            BookImage bookImage = BookImage.builder()
                    .book(book)
                    .mediaAsset(mediaAsset)
                    .sortOrder(++nextSortOrder)
                    .primaryImage(primary)
                    .altText(normalizeAltText(item.altText()))
                    .build();

            book.getImages().add(bookImage);
        }

        return toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponse setPrimaryImage(Long bookId, Long bookImageId) {
        Book book = findBook(bookId);
        BookImage target = findBookImage(bookImageId);

        validateBookImageBelongsToBook(bookId, target);

        for (BookImage image : book.getImages()) {
            image.setPrimaryImage(image.getId().equals(target.getId()));
        }

        return toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponse reorderImages(Long bookId, ReorderBookImagesRequest request) {
        Book book = findBook(bookId);

        Map<Long, BookImage> currentImages = book.getImages().stream()
                .collect(Collectors.toMap(BookImage::getId, Function.identity()));

        for (ReorderBookImageItemRequest item : request.items()) {
            BookImage bookImage = currentImages.get(item.bookImageId());
            if (bookImage == null) {
                throw new ResourceNotFoundException("Book image not found with id: " + item.bookImageId());
            }
            bookImage.setSortOrder(item.sortOrder());
        }

        return toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void removeImage(Long bookId, Long bookImageId) {
        Book book = findBook(bookId);
        BookImage target = findBookImage(bookImageId);

        validateBookImageBelongsToBook(bookId, target);

        boolean removedWasPrimary = target.isPrimaryImage();

        book.getImages().removeIf(image -> image.getId().equals(bookImageId));
        bookImageRepository.delete(target);

        if (removedWasPrimary && !book.getImages().isEmpty()) {
            BookImage nextPrimary = book.getImages().stream()
                    .min(Comparator.comparing(BookImage::getSortOrder))
                    .orElse(null);

            if (nextPrimary != null) {
                nextPrimary.setPrimaryImage(true);
            }
        }
    }

    private Specification<Book> buildSpecification(BookFilterRequest filter) {
        Set<Long> categoryIds = filter.categoryId() != null
                ? collectCategoryAndDescendantIds(filter.categoryId())
                : Set.of();

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + filter.title().trim().toLowerCase() + "%"
                ));
            }

            if (filter.authorId() != null) {
                predicates.add(cb.equal(root.get("author").get("id"), filter.authorId()));
            }

            if (!categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            if (filter.publicationYear() != null) {
                predicates.add(cb.equal(root.get("publicationYear"), filter.publicationYear()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Set<Long> collectCategoryAndDescendantIds(Long categoryId) {
        Category rootCategory = findCategory(categoryId);
        List<Category> allCategories = categoryRepository.findAll();

        Map<Long, List<Long>> childrenByParentId = new HashMap<>();
        for (Category category : allCategories) {
            if (category.getParent() == null) {
                continue;
            }

            Long parentId = category.getParent().getId();
            childrenByParentId
                    .computeIfAbsent(parentId, ignored -> new ArrayList<>())
                    .add(category.getId());
        }

        Set<Long> allIds = new HashSet<>();
        Deque<Long> toVisit = new ArrayDeque<>();
        toVisit.push(rootCategory.getId());

        while (!toVisit.isEmpty()) {
            Long currentId = toVisit.pop();
            if (!allIds.add(currentId)) {
                continue;
            }

            for (Long childId : childrenByParentId.getOrDefault(currentId, List.of())) {
                toVisit.push(childId);
            }
        }

        return allIds;
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private BookImage findBookImage(Long id) {
        return bookImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book image not found with id: " + id));
    }

    private Author findAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private MediaAsset findMediaAsset(Long id) {
        return mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with id: " + id));
    }

    private void validateUniqueIsbn(String isbn, Long currentBookId) {
        String normalized = isbn.trim();

        if (!bookRepository.existsByIsbn(normalized)) {
            return;
        }

        if (currentBookId == null) {
            throw new ConflictException("Book ISBN already exists");
        }

        Book current = findBook(currentBookId);
        if (!current.getIsbn().equalsIgnoreCase(normalized)) {
            throw new ConflictException("Book ISBN already exists");
        }
    }

    private void validateBookImageBelongsToBook(Long bookId, BookImage bookImage) {
        if (!bookImage.getBook().getId().equals(bookId)) {
            throw new ConflictException("Book image does not belong to the specified book");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private String normalizeAltText(String altText) {
        if (altText == null || altText.isBlank()) {
            return null;
        }
        return altText.trim();
    }

    private BookSummaryResponse toSummaryResponse(Book book) {
        String primaryImageUrl = book.getImages().stream()
                .filter(BookImage::isPrimaryImage)
                .map(image -> image.getMediaAsset().getSecureUrl())
                .findFirst()
                .orElse(null);

        return new BookSummaryResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getPublisher(),
                book.getPublicationYear(),
                book.getAuthor().getId(),
                book.getAuthor().getFullName(),
                book.getCategory().getId(),
                book.getCategory().getName(),
                primaryImageUrl,
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }

    private BookResponse toResponse(Book book) {
        List<BookImageResponse> images = book.getImages().stream()
                .sorted(Comparator.comparing(BookImage::getSortOrder))
                .map(image -> new BookImageResponse(
                        image.getId(),
                        image.getMediaAsset().getId(),
                        image.getMediaAsset().getSecureUrl(),
                        image.isPrimaryImage(),
                        image.getSortOrder(),
                        image.getAltText()
                ))
                .toList();

        String primaryImageUrl = images.stream()
                .filter(BookImageResponse::primaryImage)
                .map(BookImageResponse::secureUrl)
                .findFirst()
                .orElse(null);

        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getPublisher(),
                book.getPublicationYear(),
                book.getDescription(),
                book.getAuthor().getId(),
                book.getAuthor().getFullName(),
                book.getCategory().getId(),
                book.getCategory().getName(),
                primaryImageUrl,
                images,
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
