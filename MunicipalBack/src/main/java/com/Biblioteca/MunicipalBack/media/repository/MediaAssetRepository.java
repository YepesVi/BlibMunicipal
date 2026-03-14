package com.Biblioteca.MunicipalBack.media.repository;

import com.Biblioteca.MunicipalBack.media.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    Optional<MediaAsset> findByPublicId(String publicId);

    boolean existsByPublicId(String publicId);
}
