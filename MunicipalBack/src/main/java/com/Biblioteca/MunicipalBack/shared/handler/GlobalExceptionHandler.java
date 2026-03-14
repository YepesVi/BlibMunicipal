package com.Biblioteca.MunicipalBack.shared.handler;

import com.Biblioteca.MunicipalBack.shared.dto.ApiErrorResponse;
import com.Biblioteca.MunicipalBack.shared.exceptions.ConflictException;
import com.Biblioteca.MunicipalBack.shared.exceptions.InvalidCredentialsException;
import com.Biblioteca.MunicipalBack.shared.exceptions.InvalidTokenException;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import com.Biblioteca.MunicipalBack.shared.exceptions.TokenRefreshException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = new LinkedHashMap<>();
                for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
                        errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                }

                return ResponseEntity.badRequest().body(
                                new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                "Validation error",
                                                request.getRequestURI(),
                                                errors));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex,
                        HttpServletRequest request) {
                return ResponseEntity.badRequest().body(
                                new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                ex.getMessage(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler({
                        ConflictException.class,
                        IllegalArgumentException.class
        })
        public ResponseEntity<ApiErrorResponse> handleConflict(
                        RuntimeException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.CONFLICT.value(),
                                                HttpStatus.CONFLICT.getReasonPhrase(),
                                                ex.getMessage(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler({
                        InvalidCredentialsException.class,
                        BadCredentialsException.class
        })
        public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
                        RuntimeException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.UNAUTHORIZED.value(),
                                                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                                ex.getMessage(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler({
                        InvalidTokenException.class,
                        TokenRefreshException.class
        })
        public ResponseEntity<ApiErrorResponse> handleInvalidToken(
                        RuntimeException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.UNAUTHORIZED.value(),
                                                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                                ex.getMessage(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleNotFound(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.NOT_FOUND.value(),
                                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                                ex.getMessage(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneric(
                        Exception ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                "Unexpected internal server error",
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
                        org.springframework.web.multipart.MaxUploadSizeExceededException ex,
                        jakarta.servlet.http.HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                new ApiErrorResponse(
                                                java.time.LocalDateTime.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                "Uploaded file exceeds the maximum allowed size",
                                                request.getRequestURI(),
                                                null));
        }
}