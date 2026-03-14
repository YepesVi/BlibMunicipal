package com.Biblioteca.MunicipalBack.media.dto;

import java.time.LocalDateTime;

public record MediaAssetResponse(
        Long id,
        String publicId,
        String secureUrl,
        String resourceType,
        String format,
        String originalFilename,
        String contentType,
        Long sizeInBytes,
        Integer width,
        Integer height,
        LocalDateTime createdAt
) {
}