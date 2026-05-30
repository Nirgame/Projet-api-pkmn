package net.tcgdex.model;

import net.tcgdex.util.PokemonNameUtils;
import net.tcgdex.util.PokepediaUtils;

public record PokemonSpeciesInfo(
        int id,
        int speciesId,
        String slug,
        String englishName,
        String frenchName,
        int generationId,
        String generationLabel,
        RegionalForm regionalForm,
        PokemonAlternativeForm alternativeForm,
        String baseEnglishName,
        String baseFrenchName) {

    public PokemonSpeciesInfo(
            int id,
            int speciesId,
            String slug,
            String englishName,
            String frenchName,
            int generationId,
            String generationLabel,
            RegionalForm regionalForm) {
        this(id, speciesId, slug, englishName, frenchName, generationId, generationLabel, regionalForm, null, englishName, frenchName);
    }

    public boolean hasDifferentFrenchName() {
        return frenchName != null
                && !frenchName.isBlank()
                && !englishName.equalsIgnoreCase(frenchName);
    }

    public String getDisplayName() {
        if (frenchName != null && !frenchName.isBlank()) {
            return frenchName;
        }
        return getSearchableEnglishName();
    }

    public String getSecondaryName() {
        if (hasDifferentFrenchName()) {
            return getSearchableEnglishName();
        }
        return null;
    }

    public String getSearchableEnglishName() {
        return englishName != null && !englishName.isBlank()
                ? englishName
                : PokemonNameUtils.slugToDisplayName(slug);
    }

    public String getSearchableFrenchName() {
        return frenchName != null ? frenchName : "";
    }

    public boolean isRegionalForm() {
        return regionalForm != null;
    }

    public boolean isAlternativeForm() {
        return alternativeForm != null;
    }

    public boolean isMegaForm() {
        return alternativeForm != null && alternativeForm.megaForm();
    }

    public boolean isGigantamaxForm() {
        return alternativeForm != null && alternativeForm.gigantamaxForm();
    }

    public String getRegionalFormLabel() {
        return regionalForm != null ? regionalForm.label() : "";
    }

    public String getFormLabel() {
        if (alternativeForm != null) {
            return alternativeForm.formLabel();
        }
        return regionalForm != null ? regionalForm.label() : "";
    }

    public String getBaseEnglishName() {
        return baseEnglishName != null && !baseEnglishName.isBlank()
                ? baseEnglishName
                : getSearchableEnglishName();
    }

    public String getBaseFrenchName() {
        if (baseFrenchName != null && !baseFrenchName.isBlank()) {
            return baseFrenchName;
        }
        return getSearchableFrenchName();
    }

    public String getPokepediaUrl() {
        return PokepediaUtils.buildPokemonUrl(getBaseFrenchName(), getBaseEnglishName());
    }
}
