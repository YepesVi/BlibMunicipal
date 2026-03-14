package com.Biblioteca.MunicipalBack.catalog.authors.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "authors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_authors_id_card", columnNames = "id_card")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_card", nullable = false, length = 30)
    private String idCard;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "nationality", nullable = false, length = 80)
    private String nationality;

    @Column(name = "biography", length = 2000)
    private String biography;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}