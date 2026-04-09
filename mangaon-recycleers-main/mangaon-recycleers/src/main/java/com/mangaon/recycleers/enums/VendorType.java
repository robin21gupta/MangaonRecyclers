package com.mangaon.recycleers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Types of vendors based on material they supply.
 */
public enum VendorType {

    PYROLYSIS_OIL("Pyrolysis Oil"),
    CRUMB_RUBBER("Crumb Rubber"),
    WOOD("Wood"),
    OVERHEADS("Overheads");

    private final String displayName;

    VendorType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @JsonCreator
    public static VendorType fromJson(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        for (VendorType t : values()) {
            if (t.name().equalsIgnoreCase(v) || t.displayName.equalsIgnoreCase(v)) {
                return t;
            }
        }
        throw new IllegalArgumentException(
                "Unknown vendor type: '" + value + "'. Accepted: PYROLYSIS_OIL, CRUMB_RUBBER, WOOD, OVERHEADS");
    }

    @Override
    public String toString() { return displayName; }
}