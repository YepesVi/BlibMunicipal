package com.Biblioteca.MunicipalBack.media.service;

import com.Biblioteca.MunicipalBack.media.dto.MediaAssetResponse;
import com.Biblioteca.MunicipalBack.media.model.MediaFolder;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaAssetService {

    MediaAssetResponse uploadImage(MultipartFile file, MediaFolder folder);

    MediaAssetResponse findById(Long id);

    List<MediaAssetResponse> findAll();

    void deleteById(Long id);
}