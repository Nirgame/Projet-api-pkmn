package net.tcgdex.model;

public record PokemonIndexEntry(
        int id,
        int speciesId,
        String slug,
        int generationId,
        String generationLabel,
        RegionalForm regionalForm,
        PokemonAlternativeForm alternativeForm,
        String englishName,
        String frenchName,
        String formLabel) {

    public PokemonIndexEntry(
            int id,
            int speciesId,
            String slug,
            int generationId,
            String generationLabel,
            RegionalForm regionalForm) {
        this(id, speciesId, slug, generationId, generationLabel, regionalForm, null, null, null, null);
    }

    public boolean isRegionalForm() {
        return regionalForm != null;
    }

    public boolean isAlternativeForm() {
        return alternativeForm != null;
    }
}
