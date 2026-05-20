package net.tcgdex.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class PokepediaUtils {

    private PokepediaUtils() {
    }

    public static String buildPokemonUrl(String preferredName, String fallbackName) {
        String query = deriveBasePokemonName(preferredName, fallbackName);
        if (query == null || query.isBlank()) {
            query = "Pokemon";
        }

        return "https://www.pokepedia.fr/index.php?search="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    public static String deriveBasePokemonName(String preferredName, String fallbackName) {
        String registryFrench = PokemonAlternativeForms.resolveBaseFrenchName(fallbackName, preferredName);
        if (registryFrench != null && !registryFrench.isBlank()) {
            return registryFrench;
        }

        String cleanedPreferred = stripRegionalAndFormSuffix(preferredName);
        if (cleanedPreferred != null && !cleanedPreferred.isBlank()) {
            return cleanedPreferred;
        }

        String registryEnglish = PokemonAlternativeForms.resolveBaseEnglishName(fallbackName, preferredName);
        if (registryEnglish != null && !registryEnglish.isBlank()) {
            return registryEnglish;
        }

        String cleanedFallback = stripRegionalAndFormSuffix(fallbackName);
        if (cleanedFallback != null && !cleanedFallback.isBlank()) {
            return cleanedFallback;
        }

        return preferredName != null && !preferredName.isBlank() ? preferredName : fallbackName;
    }

    private static String stripRegionalAndFormSuffix(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String cleaned = value.trim();
        cleaned = cleaned.replaceFirst("^(?i)(Alolan|Galarian|Hisuian|Paldean)\\s+", "");
        cleaned = cleaned.replaceFirst("(?i)\\s+d['’]Alola.*$", "");
        cleaned = cleaned.replaceFirst("(?i)\\s+de\\s+Galar.*$", "");
        cleaned = cleaned.replaceFirst("(?i)\\s+de\\s+Hisui.*$", "");
        cleaned = cleaned.replaceFirst("(?i)\\s+de\\s+Paldea.*$", "");

        String lower = cleaned.toLowerCase(Locale.ROOT);
        if (lower.contains(" forme ")) {
            cleaned = cleaned.substring(0, lower.indexOf(" forme ")).trim();
        }
        if (lower.contains(" mode ")) {
            cleaned = cleaned.substring(0, lower.indexOf(" mode ")).trim();
        }

        return cleaned.trim();
    }
}
