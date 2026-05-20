package net.tcgdex.model;

import java.util.Arrays;
import java.util.Locale;

public enum RegionalDisplayMode {
    INCLUDE("Inclure"),
    EXCLUDE("Exclure"),
    ONLY("Seulement");

    private final String label;

    RegionalDisplayMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static RegionalDisplayMode fromFilterValue(String value) {
        if (value == null || value.isBlank()) {
            return INCLUDE;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(mode -> mode.name().equals(normalized))
                .findFirst()
                .orElse(INCLUDE);
    }
}
