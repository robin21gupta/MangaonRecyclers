package com.mangaon.recycleers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    ACCOUNT,   // Super admin — full system control
    ADMIN,     // User & content management
    OPERATOR,  // Day-to-day operations
    USER;      // Basic read access

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static Role fromJson(String value) {
        if (value == null) return null;
        String v = value.trim().toUpperCase();

        if (v.startsWith("ROLE_")) {
            v = v.substring(5);
        }

        for (Role role : values()) {
            if (role.name().equals(v)) return role;
        }

        switch (v) {
            case "MASTER":
            case "SUPERADMIN":
            case "SUPER_ADMIN": return ADMIN;
            default: throw new IllegalArgumentException(
                    "Unknown role: '" + value + "'. Accepted: ACCOUNT, ADMIN, OPERATOR, USER"
            );
        }
    }
}