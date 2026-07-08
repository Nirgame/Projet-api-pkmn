package net.tcgdex.util;

import net.tcgdex.entity.UserCard;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.PokemonSpeciesInfo;
import net.tcgdex.model.Set;
import net.tcgdex.service.UiLanguageService;
import org.springframework.stereotype.Component;

@Component("displayText")
public class DisplayTextHelper {

    private final UiLanguageService uiLanguageService;

    public DisplayTextHelper(UiLanguageService uiLanguageService) {
        this.uiLanguageService = uiLanguageService;
    }

    public boolean isEnglish() {
        return "en".equalsIgnoreCase(uiLanguageService.getCurrentLanguage());
    }

    public String text(String french, String english) {
        return isEnglish()
                ? coalesce(english, french)
                : coalesce(french, english);
    }

    public String cardName(CardBrief card) {
        if (card == null) {
            return "";
        }
        return text(card.getFrenchName(), card.getEnglishName());
    }

    public String cardSecondary(CardBrief card) {
        if (card == null) {
            return null;
        }
        return secondary(text(card.getFrenchName(), card.getEnglishName()), text(card.getEnglishName(), card.getFrenchName()));
    }

    public String userCardName(UserCard card) {
        if (card == null) {
            return "";
        }
        return text(card.getFrenchName(), card.getName());
    }

    public String userCardSecondary(UserCard card) {
        if (card == null) {
            return null;
        }
        return secondary(text(card.getFrenchName(), card.getName()), text(card.getName(), card.getFrenchName()));
    }

    public String pokemonName(PokemonSpeciesInfo species) {
        if (species == null) {
            return "";
        }
        return text(species.frenchName(), species.getSearchableEnglishName());
    }

    public String pokemonSecondary(PokemonSpeciesInfo species) {
        if (species == null) {
            return null;
        }
        return secondary(text(species.frenchName(), species.getSearchableEnglishName()),
                text(species.getSearchableEnglishName(), species.frenchName()));
    }

    public String setName(Set set) {
        if (set == null) {
            return "";
        }
        return text(set.getFrenchName(), set.getEnglishName());
    }

    public String setSecondary(Set set) {
        if (set == null) {
            return null;
        }
        return secondary(text(set.getFrenchName(), set.getEnglishName()), text(set.getEnglishName(), set.getFrenchName()));
    }

    public String cardSetName(CardBrief card) {
        if (card == null) {
            return null;
        }
        return blankToNull(coalesce(card.getDisplaySetName()));
    }

    public String userCardSetName(UserCard card) {
        if (card == null) {
            return null;
        }
        return blankToNull(coalesce(card.getDisplaySetName()));
    }

    private String secondary(String primary, String alternate) {
        String normalizedPrimary = coalesce(primary);
        String normalizedAlternate = coalesce(alternate);
        if (normalizedAlternate.isBlank() || normalizedAlternate.equalsIgnoreCase(normalizedPrimary)) {
            return null;
        }
        return normalizedAlternate;
    }

    private String coalesce(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
