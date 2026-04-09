package com.mangaon.recycleers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ClientType {

    PYROLYSIS_OIL("Pyrolysis Oil"),
    CARBON("Carbon"),
    STEEL("Steel");

    private final String displayName;

    ClientType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @JsonCreator
    public static ClientType fromJson(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim().toUpperCase();
        for (ClientType t : values()) {
            if (t.name().equalsIgnoreCase(v) || t.displayName.equalsIgnoreCase(value.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown client type: '" + value + "'. Accepted: PYROLYSIS_OIL, CARBON, STEEL");
    }

    @Override
    public String toString() { return displayName; }
}