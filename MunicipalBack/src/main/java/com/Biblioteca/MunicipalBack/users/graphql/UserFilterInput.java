package com.Biblioteca.MunicipalBack.users.graphql;

public record UserFilterInput(
        String username,
        String role,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir
) {
    public UserFilterInput {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sortBy == null) sortBy = "username";
        if (sortDir == null) sortDir = "asc";
    }
}
