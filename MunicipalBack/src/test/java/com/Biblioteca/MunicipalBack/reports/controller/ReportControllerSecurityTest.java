package com.Biblioteca.MunicipalBack.reports.controller;

import com.Biblioteca.MunicipalBack.auth.config.SecurityConfig;
import com.Biblioteca.MunicipalBack.auth.security.JwtAuthenticationFilter;
import com.Biblioteca.MunicipalBack.auth.security.RestAccessDeniedHandler;
import com.Biblioteca.MunicipalBack.auth.security.RestAuthenticationEntryPoint;
import com.Biblioteca.MunicipalBack.auth.service.CustomUserDetailsService;
import com.Biblioteca.MunicipalBack.reports.dto.BooksByAuthorReportResponse;
import com.Biblioteca.MunicipalBack.reports.service.ReportService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain filterChain = invocation.getArgument(2);
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }).when(restAuthenticationEntryPoint).commence(any(), any(), any());

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }).when(restAccessDeniedHandler).handle(any(), any(), any());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/reports/books/by-author-id-card/0102"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanAccessPreview() throws Exception {
        when(reportService.getBooksByAuthorIdCard("0102")).thenReturn(
                new BooksByAuthorReportResponse("0102", "Author", null, 0, List.of())
        );

        mockMvc.perform(get("/api/reports/books/by-author-id-card/0102"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessPdf() throws Exception {
        when(reportService.getBooksByAuthorIdCardPdf("0102")).thenReturn("%PDF".getBytes());

        mockMvc.perform(get("/api/reports/books/by-author-id-card/0102/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void unsupportedRoleIsForbidden() throws Exception {
        mockMvc.perform(get("/api/reports/books/by-author-id-card/0102"))
                .andExpect(status().isForbidden());
    }
}
