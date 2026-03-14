package com.Biblioteca.MunicipalBack.media.model;

import java.util.Arrays;

public enum MediaContentType {
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_WEBP("image/webp");

    private final String value;

    MediaContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isAllowed(String contentType) {
        return Arrays.stream(values())
                .anyMatch(type -> type.value.equalsIgnoreCase(contentType));
    }
}