package com.mangaon.recycleers.dto;

import com.mangaon.recycleers.enums.Role;
import com.mangaon.recycleers.model.User;

public record UserResponse(
        Long id,
        String username,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}