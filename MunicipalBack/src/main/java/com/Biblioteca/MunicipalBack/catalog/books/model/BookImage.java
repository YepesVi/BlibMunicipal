package com.Biblioteca.MunicipalBack.catalog.books.model;

import com.Biblioteca.MunicipalBack.media.model.MediaAsset;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "book_images",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_book_images_book_media", columnNames = {"book_id", "media_asset_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_book_image_book"))
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_asset_id", nullable = false, foreignKey = @ForeignKey(name = "fk_book_image_media"))
    private MediaAsset mediaAsset;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "primary_image", nullable = false)
    private boolean primaryImage;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}