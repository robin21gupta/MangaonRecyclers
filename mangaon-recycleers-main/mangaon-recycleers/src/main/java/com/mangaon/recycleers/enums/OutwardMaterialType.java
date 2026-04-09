package com.mangaon.recycleers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OutwardMaterialType {

    TYRE_OIL("Pyrolysis Oil"),
    CARBON_POWER("Carbon Powder"),
    STEEL("Steel");

    private final String displayName;

    OutwardMaterialType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static OutwardMaterialType fromJson(String value) {
        if (value == null) return null;
        String v = value.trim();

        // Match by enum name (e.g. TYRE_OIL, CARBON_POWER, STEEL)
        for (OutwardMaterialType type : values()) {
            if (type.name().equalsIgnoreCase(v)) return type;
        }

        // Match by display name (e.g. "Pyrolysis Oil", "Carbon Powder")
        for (OutwardMaterialType type : values()) {
            if (type.displayName.equalsIgnoreCase(v)) return type;
        }

        // Legacy alias — in case old data/frontend sends "PYROLYSIS_OIL"
        if (v.equalsIgnoreCase("PYROLYSIS_OIL") || v.equalsIgnoreCase("Tyre Oil")) {
            return TYRE_OIL;
        }

        throw new IllegalArgumentException(
                "Unknown material type: '" + value + "'. Accepted: TYRE_OIL, CARBON_POWER, STEEL"
        );
    }

    public static OutwardMaterialType fromName(String name) {
        if (name == null) return null;
        for (OutwardMaterialType type : values()) {
            if (type.name().equalsIgnoreCase(name.trim())) return type;
        }
        // Legacy alias
        if (name.trim().equalsIgnoreCase("PYROLYSIS_OIL")) return TYRE_OIL;
        return null;
    }

    @Override
    public String toString() { return displayName; }
}