package net.tcgdex.model;

import java.util.Arrays;
import java.util.Locale;

public enum RegionalForm {
    ALOLA(1, 7, "Alola", "d'Alola", "alolan"),
    GALAR(2, 8, "Galar", "de Galar", "galarian"),
    HISUI(3, 8, "Hisui", "de Hisui", "hisuian"),
    PALDEA(4, 9, "Paldea", "de Paldea", "paldean");

    private static final int ENTRY_ID_PREFIX = 1_000_000;
    private static final int ENTRY_ID_STEP = 10_000;

    private final int code;
    private final int appearanceGenerationId;
    private final String label;
    private final String frenchSuffix;
    private final String englishPrefix;

    RegionalForm(int code, int appearanceGenerationId, String label, String frenchSuffix, String englishPrefix) {
        this.code = code;
        this.appearanceGenerationId = appearanceGenerationId;
        this.label = label;
        this.frenchSuffix = frenchSuffix;
        this.englishPrefix = englishPrefix;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public int appearanceGenerationId() {
        return appearanceGenerationId;
    }

    public String frenchSuffix() {
        return frenchSuffix;
    }

    public String englishPrefix() {
        return englishPrefix;
    }

    public int toEntryId(int speciesId) {
        return ENTRY_ID_PREFIX + (code * ENTRY_ID_STEP) + speciesId;
    }

    public static boolean isRegionalEntryId(int entryId) {
        return entryId >= ENTRY_ID_PREFIX;
    }

    public static int extractSpeciesId(int entryId) {
        if (!isRegionalEntryId(entryId)) {
            return entryId;
        }
        return entryId % ENTRY_ID_STEP;
    }

    public static RegionalForm fromEntryId(int entryId) {
        if (!isRegionalEntryId(entryId)) {
            return null;
        }

        int resolvedCode = (entryId - ENTRY_ID_PREFIX) / ENTRY_ID_STEP;
        return Arrays.stream(values())
                .filter(form -> form.code == resolvedCode)
                .findFirst()
                .orElse(null);
    }

    public static RegionalForm fromFilterValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(form -> form.name().equals(normalized))
                .findFirst()
                .orElse(null);
    }
}
