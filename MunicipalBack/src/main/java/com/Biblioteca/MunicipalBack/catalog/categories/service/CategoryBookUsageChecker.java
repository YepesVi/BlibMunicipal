package com.Biblioteca.MunicipalBack.catalog.categories.service;

public interface CategoryBookUsageChecker {
    boolean hasBooksAssociated(Long categoryId);
}
