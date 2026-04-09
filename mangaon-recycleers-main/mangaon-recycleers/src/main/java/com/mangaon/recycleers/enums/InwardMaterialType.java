package com.mangaon.recycleers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InwardMaterialType {

    PYROLYSIS_OIL("Pyrolysis Oil"),
    CRUMB_RUBBER("Crumb Rubber"),
    WOOD("Wood"),
    OTHERS("Others");

    private final String displayName;

    InwardMaterialType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static InwardMaterialType fromJson(String value) {
        if (value == null) return null;
        String v = value.trim();

        // Match by enum name
        for (InwardMaterialType type : values()) {
            if (type.name().equalsIgnoreCase(v)) return type;
        }

        // Match by display name
        for (InwardMaterialType type : values()) {
            if (type.displayName.equalsIgnoreCase(v)) return type;
        }

        // Legacy aliases
        switch (v.toUpperCase()) {
            case "TYRE":          return PYROLYSIS_OIL;
            case "TYRE_INWARD":   return PYROLYSIS_OIL;
            case "CRUMB":         return CRUMB_RUBBER;
            case "WOOD_INWARD":   return WOOD;
            case "OTHERS_INWARD": return OTHERS;
            default: throw new IllegalArgumentException(
                    "Unknown inward material type: '" + value +
                            "'. Accepted: PYROLYSIS_OIL, CRUMB_RUBBER, WOOD, OTHERS"
            );
        }
    }

    public static InwardMaterialType fromName(String name) {
        if (name == null) return null;
        String v = name.trim();
        for (InwardMaterialType type : values()) {
            if (type.name().equalsIgnoreCase(v)) return type;
        }
        switch (v.toUpperCase()) {
            case "TYRE":          return PYROLYSIS_OIL;
            case "TYRE_INWARD":   return PYROLYSIS_OIL;
            case "WOOD_INWARD":   return WOOD;
            case "OTHERS_INWARD": return OTHERS;
            case "CRUMB":         return CRUMB_RUBBER;
            default:              return null;
        }
    }

    @Override
    public String toString() { return displayName; }
}