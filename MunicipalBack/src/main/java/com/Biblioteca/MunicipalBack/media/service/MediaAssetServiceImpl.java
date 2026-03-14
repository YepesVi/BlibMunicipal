package com.Biblioteca.MunicipalBack.media.service;

import com.Biblioteca.MunicipalBack.media.dto.MediaAssetResponse;
import com.Biblioteca.MunicipalBack.media.model.MediaAsset;
import com.Biblioteca.MunicipalBack.media.model.MediaContentType;
import com.Biblioteca.MunicipalBack.media.model.MediaFolder;
import com.Biblioteca.MunicipalBack.media.repository.MediaAssetRepository;
import com.Biblioteca.MunicipalBack.shared.exceptions.ConflictException;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaAssetServiceImpl implements MediaAssetService {

    private final Cloudinary cloudinary;
    private final MediaAssetRepository mediaAssetRepository;
    private final MediaAssetUsageChecker mediaAssetUsageChecker;

    @Value("${cloudinary.base-folder}")
    private String baseFolder;

    @Value("${media.max-file-size-bytes}")
    private long maxFileSizeBytes;

    @Override
    @Transactional
    public MediaAssetResponse uploadImage(MultipartFile file, MediaFolder folder) {
        validateFile(file);

        String finalFolder = buildFolder(folder);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "resource_type", "image",
                            "folder", finalFolder,
                            "overwrite", false,
                            "unique_filename", true,
                            "use_filename", false
                    )
            );

            String publicId = Objects.toString(uploadResult.get("public_id"), null);
            String secureUrl = Objects.toString(uploadResult.get("secure_url"), null);

            if (publicId == null || publicId.isBlank()) {
                throw new ConflictException("Cloudinary did not return a valid public id");
            }

            if (secureUrl == null || secureUrl.isBlank()) {
                throw new ConflictException("Cloudinary did not return a valid secure url");
            }

            if (mediaAssetRepository.existsByPublicId(publicId)) {
                throw new ConflictException("A media asset with the same public id already exists");
            }

            MediaAsset mediaAsset = MediaAsset.builder()
                    .publicId(publicId)
                    .secureUrl(secureUrl)
                    .resourceType(Objects.toString(uploadResult.get("resource_type"), "image"))
                    .format(Objects.toString(uploadResult.get("format"), null))
                    .originalFilename(normalizeOriginalFilename(file.getOriginalFilename()))
                    .contentType(file.getContentType())
                    .sizeInBytes(extractLong(uploadResult.get("bytes")))
                    .width(extractInteger(uploadResult.get("width")))
                    .height(extractInteger(uploadResult.get("height")))
                    .build();

            return toResponse(mediaAssetRepository.save(mediaAsset));

        } catch (IOException ex) {
            throw new ConflictException("Failed to read uploaded file");
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ConflictException("Failed to upload image to Cloudinary");
        }
    }

    @Override
    public MediaAssetResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    public List<MediaAssetResponse> findAll() {
        return mediaAssetRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(MediaAsset::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        MediaAsset mediaAsset = findEntity(id);

        if (mediaAssetUsageChecker.isInUse(id)) {
            throw new ConflictException("Cannot delete media asset because it is currently in use");
        }

        try {
            cloudinary.uploader().destroy(
                    mediaAsset.getPublicId(),
                    Map.of("resource_type", mediaAsset.getResourceType())
            );
        } catch (Exception ex) {
            throw new ConflictException("Failed to delete image from Cloudinary");
        }

        mediaAssetRepository.delete(mediaAsset);
    }

    private MediaAsset findEntity(Long id) {
        return mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with id: " + id));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ConflictException("Image file is required");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new ConflictException("Uploaded image exceeds the allowed size");
        }

        String contentType = file.getContentType();
        if (contentType == null || !MediaContentType.isAllowed(contentType)) {
            throw new ConflictException("Only JPEG, PNG and WEBP images are allowed");
        }
    }

    private String buildFolder(MediaFolder folder) {
        if (folder == null) {
            throw new ConflictException("Target folder is required");
        }

        String normalizedBase = normalizeFolderSegment(baseFolder);
        String normalizedFolder = normalizeFolderSegment(folder.getFolderName());

        if (normalizedBase.isBlank()) {
            return normalizedFolder;
        }

        return normalizedBase + "/" + normalizedFolder;
    }

    private String normalizeFolderSegment(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace("\\", "/")
                .replaceAll("/+", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .toLowerCase();
    }

    private String normalizeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }

        return originalFilename.trim()
                .replace("\\", "_")
                .replace("/", "_");
    }

    private Long extractLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }

    private Integer extractInteger(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }

    private MediaAssetResponse toResponse(MediaAsset mediaAsset) {
        return new MediaAssetResponse(
                mediaAsset.getId(),
                mediaAsset.getPublicId(),
                mediaAsset.getSecureUrl(),
                mediaAsset.getResourceType(),
                mediaAsset.getFormat(),
                mediaAsset.getOriginalFilename(),
                mediaAsset.getContentType(),
                mediaAsset.getSizeInBytes(),
                mediaAsset.getWidth(),
                mediaAsset.getHeight(),
                mediaAsset.getCreatedAt()
        );
    }
}