package com.Biblioteca.MunicipalBack.catalog.authors.service;

public interface AuthorBookUsageChecker {
    boolean hasBooksAssociated(Long authorId);
}