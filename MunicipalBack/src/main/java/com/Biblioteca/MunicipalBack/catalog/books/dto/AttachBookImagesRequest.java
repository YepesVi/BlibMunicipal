package com.Biblioteca.MunicipalBack.catalog.books.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AttachBookImagesRequest(

        @NotEmpty(message = "At least one image is required")
        @Valid
        List<BookImageAttachItem> images
) {
    public record BookImageAttachItem(
            @NotNull(message = "Media asset id is required")
            Long mediaAssetId,

            Boolean primaryImage,

            @Size(max = 255, message = "Alt text must not exceed 255 characters")
            String altText
    ) {
    }
}