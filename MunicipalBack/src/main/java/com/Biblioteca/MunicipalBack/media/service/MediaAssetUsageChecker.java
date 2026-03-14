package com.Biblioteca.MunicipalBack.media.service;

public interface MediaAssetUsageChecker {
    boolean isInUse(Long mediaAssetId);
}