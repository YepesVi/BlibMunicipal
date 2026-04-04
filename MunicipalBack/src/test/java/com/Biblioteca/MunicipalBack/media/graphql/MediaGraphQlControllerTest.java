package com.Biblioteca.MunicipalBack.media.graphql;

import com.Biblioteca.MunicipalBack.media.dto.MediaAssetResponse;
import com.Biblioteca.MunicipalBack.media.service.MediaAssetService;
import com.Biblioteca.MunicipalBack.shared.graphql.GraphQlExceptionResolver;
import com.Biblioteca.MunicipalBack.shared.graphql.GraphQlScalarConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@GraphQlTest(MediaGraphQlController.class)
@Import({GraphQlScalarConfig.class, GraphQlExceptionResolver.class})
@EnableMethodSecurity
class MediaGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private MediaAssetService mediaAssetService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void mediaAssets_returnsList() {
        var now = LocalDateTime.now();
        var asset = new MediaAssetResponse(1L, "public-id-1", "https://cdn.example.com/img.jpg",
                "image", "jpg", "cover.jpg", "image/jpeg", 102400L, 800, 600, now);

        when(mediaAssetService.findAll()).thenReturn(List.of(asset));

        graphQlTester.document("""
                        query {
                            mediaAssets {
                                id publicId secureUrl format originalFilename
                            }
                        }
                        """)
                .execute()
                .path("mediaAssets[0].publicId").entity(String.class).isEqualTo("public-id-1");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void mediaAsset_returnsById() {
        var now = LocalDateTime.now();
        var asset = new MediaAssetResponse(1L, "pub-id", "https://cdn.example.com/img.jpg",
                "image", "png", "photo.png", "image/png", 204800L, 1024, 768, now);

        when(mediaAssetService.findById(1L)).thenReturn(asset);

        graphQlTester.document("""
                        query {
                            mediaAsset(id: 1) {
                                id publicId secureUrl resourceType width height
                            }
                        }
                        """)
                .execute()
                .path("mediaAsset.publicId").entity(String.class).isEqualTo("pub-id")
                .path("mediaAsset.width").entity(Integer.class).isEqualTo(1024);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMediaAsset_returnsTrue() {
        graphQlTester.document("""
                        mutation {
                            deleteMediaAsset(id: 1)
                        }
                        """)
                .execute()
                .path("deleteMediaAsset").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void mediaAssets_forbiddenForEmployee() {
        graphQlTester.document("""
                        query {
                            mediaAssets {
                                id
                            }
                        }
                        """)
                .execute()
                .errors()
                .expect(error -> error.getMessage() != null && error.getMessage().contains("Access denied"));
    }
}
