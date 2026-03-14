package com.Biblioteca.MunicipalBack.media.model;

public enum MediaFolder {
    BOOKS("books"),
    USERS("users"),
    AUTHORS("authors"),
    CATEGORIES("categories");

    private final String folderName;

    MediaFolder(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }
}
