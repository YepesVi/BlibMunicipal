package com.Biblioteca.MunicipalBack.media.graphql;

import com.Biblioteca.MunicipalBack.media.dto.MediaAssetResponse;
import com.Biblioteca.MunicipalBack.media.service.MediaAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MediaGraphQlController {

    private final MediaAssetService mediaAssetService;

    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<MediaAssetResponse> mediaAssets() {
        return mediaAssetService.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MediaAssetResponse mediaAsset(@Argument Long id) {
        return mediaAssetService.findById(id);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteMediaAsset(@Argument Long id) {
        mediaAssetService.deleteById(id);
        return true;
    }
}
