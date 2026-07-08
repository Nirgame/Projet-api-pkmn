package net.tcgdex.util;

import net.tcgdex.model.CardBrief;
import net.tcgdex.model.PokemonAlternativeForm;
import net.tcgdex.model.PokemonSpeciesInfo;
import net.tcgdex.model.RegionalForm;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class PokemonNameUtils {

    private PokemonNameUtils() {
    }

    public static String normalizePokemonName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = value
                .replace("♀", " female ")
                .replace("♂", " male ")
                .replace("farfetch'd", "farfetchd")
                .replace("sirfetch'd", "sirfetchd")
                .replace("mr.", "mr")
                .replace("mime jr.", "mime jr")
                .replace("jr.", "jr");

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace("d alola", "")
                .replace("d'alola", "")
                .replace("de galar", "")
                .replace("de hisui", "")
                .replace("de paldea", "")
                .replace("alolan", "")
                .replace("galarian", "")
                .replace("hisuian", "")
                .replace("paldean", "")
                .replace("forme attaque", "")
                .replace("forme defense", "")
                .replace("forme vitesse", "")
                .replace("forme normale", "")
                .replace("attack forme", "")
                .replace("defense forme", "")
                .replace("speed forme", "")
                .replace("normal forme", "")
                .replace("mow", "")
                .replace("heat", "")
                .replace("wash", "")
                .replace("frost", "")
                .replace("fan", "")
                .replace("tonte", "")
                .replace("chaleur", "")
                .replace("lavage", "")
                .replace("gel", "")
                .replace("helice", "")
                .replace("soleil", "")
                .replace("pluie", "")
                .replace("neige", "")
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return normalized;
    }

    public static String slugToDisplayName(String slug) {
        if (slug == null || slug.isBlank()) {
            return "";
        }

        return switch (slug) {
            case "nidoran-f" -> "Nidoran♀";
            case "nidoran-m" -> "Nidoran♂";
            case "mr-mime" -> "Mr. Mime";
            case "mime-jr" -> "Mime Jr.";
            case "mr-rime" -> "Mr. Rime";
            case "farfetchd" -> "Farfetch'd";
            case "sirfetchd" -> "Sirfetch'd";
            case "ho-oh" -> "Ho-Oh";
            case "porygon-z" -> "Porygon-Z";
            case "jangmo-o" -> "Jangmo-o";
            case "hakamo-o" -> "Hakamo-o";
            case "kommo-o" -> "Kommo-o";
            case "wo-chien" -> "Wo-Chien";
            case "chien-pao" -> "Chien-Pao";
            case "ting-lu" -> "Ting-Lu";
            case "chi-yu" -> "Chi-Yu";
            default -> {
                String[] parts = slug.split("-");
                StringBuilder builder = new StringBuilder();
                for (String part : parts) {
                    if (builder.length() > 0) {
                        builder.append(' ');
                    }
                    if (!part.isBlank()) {
                        builder.append(Character.toUpperCase(part.charAt(0)));
                        if (part.length() > 1) {
                            builder.append(part.substring(1));
                        }
                    }
                }
                yield builder.toString();
            }
        };
    }

    public static boolean matchesSpecies(CardBrief card, PokemonSpeciesInfo species) {
        if (!matchesBaseName(card, species)) {
            return false;
        }

        if (species.isAlternativeForm()) {
            PokemonAlternativeForm alternativeForm = species.alternativeForm();
            if (alternativeForm.regionalForm() != null && !hasRegionalMarker(card, alternativeForm.regionalForm())) {
                return false;
            }
            if (alternativeForm.regionalForm() == null && hasAnyRegionalMarker(card)) {
                return false;
            }

            boolean currentMarker = PokemonAlternativeForms.matchesForm(
                    card.getEnglishName(),
                    card.getFrenchName(),
                    card.getFormLabel(),
                    card.getVariantLabel(),
                    alternativeForm);

            if (alternativeForm.isDefaultForm()) {
                return currentMarker || !PokemonAlternativeForms.hasOtherMarker(
                        card.getEnglishName(),
                        card.getFrenchName(),
                        card.getFormLabel(),
                        card.getVariantLabel(),
                        alternativeForm);
            }

            return currentMarker;
        }

        String cardVariant = normalizePokemonName(card.getVariantLabel());
        String expectedVariant = normalizePokemonName(species.regionalForm() != null ? species.regionalForm().label() : null);

        if (species.isRegionalForm()) {
            return (!expectedVariant.isBlank() && expectedVariant.equals(cardVariant))
                    || hasRegionalMarker(card, species.regionalForm());
        }

        return matchesBaseSpecies(card, species);
    }

    public static boolean matchesBaseSpecies(CardBrief card, PokemonSpeciesInfo species) {
        if (!matchesBaseName(card, species)) {
            return false;
        }

        if (species.isAlternativeForm() && species.alternativeForm().regionalForm() != null) {
            return hasRegionalMarker(card, species.alternativeForm().regionalForm());
        }

        if (species.isRegionalForm()) {
            return hasRegionalMarker(card, species.regionalForm());
        }

        RegionalForm intrinsicRegionalForm = getIntrinsicRegionalForm(species);
        if (intrinsicRegionalForm != null) {
            return !hasAnyRegionalMarker(card) || hasRegionalMarker(card, intrinsicRegionalForm);
        }

        return !hasAnyRegionalMarker(card);
    }

    private static boolean matchesBaseName(CardBrief card, PokemonSpeciesInfo species) {
        Set<String> speciesNames = new LinkedHashSet<>();
        speciesNames.add(normalizePokemonName(species.getBaseEnglishName()));
        speciesNames.add(normalizePokemonName(species.getBaseFrenchName()));
        speciesNames.add(normalizePokemonName(species.getSearchableEnglishName()));
        speciesNames.add(normalizePokemonName(species.getSearchableFrenchName()));

        Set<String> cardNames = new LinkedHashSet<>();
        cardNames.add(normalizePokemonName(card.getEnglishName()));
        cardNames.add(normalizePokemonName(card.getFrenchName()));

        boolean nameMatches = cardNames.stream()
                .filter(name -> !name.isBlank())
                .anyMatch(cardName -> speciesNames.stream()
                        .filter(speciesName -> !speciesName.isBlank())
                        .anyMatch(speciesName -> containsNamePhrase(cardName, speciesName)));
        return nameMatches;
    }

    private static boolean hasRegionalMarker(CardBrief card, RegionalForm regionalForm) {
        if (regionalForm == null) {
            return false;
        }

        String expectedMarker = CardNameUtils.normalizeForSearch(regionalForm.label());
        String haystack = buildRegionalHaystack(card);
        return !expectedMarker.isBlank() && haystack.contains(expectedMarker);
    }

    private static boolean hasAnyRegionalMarker(CardBrief card) {
        String haystack = buildRegionalHaystack(card);
        for (RegionalForm regionalForm : RegionalForm.values()) {
            String marker = CardNameUtils.normalizeForSearch(regionalForm.label());
            if (!marker.isBlank() && haystack.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private static String buildRegionalHaystack(CardBrief card) {
        return String.join(" ",
                CardNameUtils.normalizeForSearch(card.getEnglishName()),
                CardNameUtils.normalizeForSearch(card.getFrenchName()),
                CardNameUtils.normalizeForSearch(card.getFormLabel()),
                CardNameUtils.normalizeForSearch(card.getVariantLabel()));
    }

    private static boolean containsNamePhrase(String haystack, String phrase) {
        if (haystack == null || haystack.isBlank() || phrase == null || phrase.isBlank()) {
            return false;
        }

        String paddedHaystack = " " + haystack.trim() + " ";
        String paddedPhrase = " " + phrase.trim() + " ";
        return paddedHaystack.contains(paddedPhrase);
    }

    private static RegionalForm getIntrinsicRegionalForm(PokemonSpeciesInfo species) {
        return switch (species.speciesId()) {
            case 862, 863, 864, 865, 866, 867 -> RegionalForm.GALAR;
            case 899, 900, 901, 902, 903, 904, 905 -> RegionalForm.HISUI;
            case 980 -> RegionalForm.PALDEA;
            default -> null;
        };
    }
}
