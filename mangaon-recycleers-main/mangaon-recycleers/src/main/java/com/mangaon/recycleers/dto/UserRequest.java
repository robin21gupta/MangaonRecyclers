package com.mangaon.recycleers.dto;

import com.mangaon.recycleers.enums.Role;

public record UserRequest(
        String username,
        String password,
        Role role
) {}