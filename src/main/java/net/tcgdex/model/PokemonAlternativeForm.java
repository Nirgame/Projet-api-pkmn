package net.tcgdex.model;

import java.util.List;

public record PokemonAlternativeForm(
        int speciesId,
        int localCode,
        String englishName,
        String frenchName,
        String formLabel,
        RegionalForm regionalForm,
        boolean replacesBaseEntry,
        List<String> aliases,
        boolean megaForm,
        boolean gigantamaxForm) {

    private static final int ENTRY_ID_PREFIX = 2_000_000;
    private static final int SPECIES_MULTIPLIER = 100;

    public PokemonAlternativeForm(
            int speciesId,
            int localCode,
            String englishName,
            String frenchName,
            String formLabel,
            boolean replacesBaseEntry,
            List<String> aliases) {
        this(speciesId, localCode, englishName, frenchName, formLabel, null, replacesBaseEntry, aliases, false, false);
    }

    public PokemonAlternativeForm(
            int speciesId,
            int localCode,
            String englishName,
            String frenchName,
            String formLabel,
            RegionalForm regionalForm,
            boolean replacesBaseEntry,
            List<String> aliases) {
        this(speciesId, localCode, englishName, frenchName, formLabel, regionalForm, replacesBaseEntry, aliases, false, false);
    }

    public int toEntryId() {
        return ENTRY_ID_PREFIX + (speciesId * SPECIES_MULTIPLIER) + localCode;
    }

    public boolean isDefaultForm() {
        return replacesBaseEntry;
    }

    public boolean isMegaOrGigantamaxForm() {
        return megaForm || gigantamaxForm;
    }

    public static boolean isAlternativeEntryId(int entryId) {
        return entryId >= ENTRY_ID_PREFIX;
    }

    public static int extractSpeciesId(int entryId) {
        if (!isAlternativeEntryId(entryId)) {
            return entryId;
        }
        return (entryId - ENTRY_ID_PREFIX) / SPECIES_MULTIPLIER;
    }

    public static int extractLocalCode(int entryId) {
        if (!isAlternativeEntryId(entryId)) {
            return 0;
        }
        return (entryId - ENTRY_ID_PREFIX) % SPECIES_MULTIPLIER;
    }
}
