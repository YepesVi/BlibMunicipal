package com.Biblioteca.MunicipalBack.media.controller;

import com.Biblioteca.MunicipalBack.media.dto.MediaAssetResponse;
import com.Biblioteca.MunicipalBack.media.model.MediaFolder;
import com.Biblioteca.MunicipalBack.media.service.MediaAssetService;
import com.Biblioteca.MunicipalBack.shared.exceptions.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaAssetService mediaAssetService;

    @Value("${media.allow-public-management:false}")
    private boolean allowPublicManagement;

    @PostMapping(value = "/images", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaAssetResponse uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("folder") MediaFolder folder
    ) {
        ensurePublicManagementEnabled();
        return mediaAssetService.uploadImage(file, folder);
    }

    @GetMapping
    public List<MediaAssetResponse> findAll() {
        ensurePublicManagementEnabled();
        return mediaAssetService.findAll();
    }

    @GetMapping("/{id}")
    public MediaAssetResponse findById(@PathVariable Long id) {
        ensurePublicManagementEnabled();
        return mediaAssetService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        ensurePublicManagementEnabled();
        mediaAssetService.deleteById(id);
    }

    private void ensurePublicManagementEnabled() {
        if (!allowPublicManagement) {
            throw new ConflictException("Public media management is disabled");
        }
    }
}