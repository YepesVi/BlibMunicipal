package com.Biblioteca.MunicipalBack.shared.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}