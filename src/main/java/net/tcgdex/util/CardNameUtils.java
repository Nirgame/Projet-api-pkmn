package net.tcgdex.util;

import net.tcgdex.entity.UserCard;
import net.tcgdex.model.Card;
import net.tcgdex.model.CardBrief;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

public final class CardNameUtils {

    private CardNameUtils() {
    }

    public static String normalizeForSearch(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace("alolan", "alola")
                .replace("galarian", "galar")
                .replace("hisuian", "hisui")
                .replace("paldean", "paldea")
                .replace("gigantamax", "gigamax")
                .replace("g-max", "gigamax")
                .replaceAll("[^a-z0-9]+", " ")
                .trim();

        return normalized.replaceAll("\\s+", " ");
    }

    public static boolean matchesSearch(CardBrief card, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String haystack = String.join(" ",
                normalizeForSearch(card.getEnglishName()),
                normalizeForSearch(card.getFrenchName()),
                normalizeForSearch(card.getFormLabel()),
                normalizeForSearch(card.getVariantLabel()),
                normalizeForSearch(card.getId()),
                normalizeForSearch(card.getLocalId()));

        return containsAllTokens(haystack, normalizeForSearch(query));
    }

    public static boolean matchesSearch(UserCard card, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String haystack = String.join(" ",
                normalizeForSearch(card.getName()),
                normalizeForSearch(card.getFrenchName()),
                normalizeForSearch(card.getFormLabel()),
                normalizeForSearch(card.getVariantLabel()),
                normalizeForSearch(card.getCardId()));

        return containsAllTokens(haystack, normalizeForSearch(query));
    }

    public static boolean matchesExactName(CardBrief card, String query) {
        if (query == null || query.isBlank()) {
            return false;
        }

        String normalizedQuery = normalizeForSearch(query);
        return normalizedQuery.equals(normalizeForSearch(card.getEnglishName()))
                || normalizedQuery.equals(normalizeForSearch(card.getFrenchName()));
    }

    public static boolean matchesExactName(UserCard card, String query) {
        if (query == null || query.isBlank()) {
            return false;
        }

        String normalizedQuery = normalizeForSearch(query);
        return normalizedQuery.equals(normalizeForSearch(card.getName()))
                || normalizedQuery.equals(normalizeForSearch(card.getFrenchName()));
    }

    public static boolean matchesExactCard(UserCard card, String query, String formLabel) {
        if (!matchesExactName(card, query)) {
            return false;
        }
        if (formLabel == null || formLabel.isBlank()) {
            return true;
        }
        return normalizeForSearch(formLabel).equals(normalizeForSearch(card.getFormLabel()));
    }

    public static String extractVariantLabel(String englishName, String frenchName) {
        String normalizedEnglish = normalizeForSearch(englishName);
        String normalizedFrench = normalizeForSearch(frenchName);

        if (normalizedEnglish.contains("alola") || normalizedFrench.contains("alola")) {
            return "Alola";
        }
        if (normalizedEnglish.contains("galar") || normalizedFrench.contains("galar")) {
            return "Galar";
        }
        if (normalizedEnglish.contains("hisui") || normalizedFrench.contains("hisui")) {
            return "Hisui";
        }
        if (normalizedEnglish.contains("paldea") || normalizedFrench.contains("paldea")) {
            return "Paldea";
        }
        return null;
    }

    public static String extractNamedFormLabel(String englishName, String frenchName) {
        String registryMatch = PokemonAlternativeForms.extractFormLabel(englishName, frenchName);
        if (registryMatch != null) {
            return registryMatch;
        }

        String normalizedEnglish = normalizeForSearch(englishName);
        String normalizedFrench = normalizeForSearch(frenchName);
        String haystack = normalizedEnglish + " " + normalizedFrench;

        if (haystack.contains("mow rotom") || haystack.contains("motisma tonte")) {
            return "Tonte";
        }
        if (haystack.contains("heat rotom") || haystack.contains("motisma chaleur")) {
            return "Chaleur";
        }
        if (haystack.contains("wash rotom") || haystack.contains("motisma lavage")) {
            return "Lavage";
        }
        if (haystack.contains("frost rotom") || haystack.contains("motisma gel")) {
            return "Gel";
        }
        if (haystack.contains("fan rotom") || haystack.contains("motisma helice")) {
            return "Helice";
        }
        if (haystack.contains("deoxys attack forme") || haystack.contains("deoxys forme attaque")) {
            return "Attaque";
        }
        if (haystack.contains("deoxys defense forme") || haystack.contains("deoxys forme defense")) {
            return "Defense";
        }
        if (haystack.contains("deoxys speed forme") || haystack.contains("deoxys forme vitesse")) {
            return "Vitesse";
        }
        if (haystack.contains("deoxys normal forme") || haystack.contains("deoxys forme normale")) {
            return "Normale";
        }
        if (haystack.contains("castform sunny form") || haystack.contains("sunny castform") || haystack.contains("morpheo soleil")) {
            return "Soleil";
        }
        if (haystack.contains("castform rainy form") || haystack.contains("rain castform") || haystack.contains("morpheo pluie")) {
            return "Pluie";
        }
        if (haystack.contains("castform snowy form") || haystack.contains("snow cloud castform") || haystack.contains("morpheo neige")) {
            return "Neige";
        }
        return null;
    }

    public static String inferFormLabel(Card card) {
        return extractNamedFormLabel(card.getEnglishName(), card.getFrenchName());
    }

    private static boolean containsAllTokens(String haystack, String normalizedQuery) {
        return Arrays.stream(normalizedQuery.split(" "))
                .filter(token -> !token.isBlank())
                .allMatch(haystack::contains);
    }
}
