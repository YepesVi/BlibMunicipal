package com.Biblioteca.MunicipalBack.catalog.books.service;

import com.Biblioteca.MunicipalBack.catalog.books.repository.BookImageRepository;
import com.Biblioteca.MunicipalBack.media.service.MediaAssetUsageChecker;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class MediaAssetUsageCheckerImpl implements MediaAssetUsageChecker {

    private final BookImageRepository bookImageRepository;

    @Override
    public boolean isInUse(Long mediaAssetId) {
        return bookImageRepository.existsByMediaAssetId(mediaAssetId);
    }
}