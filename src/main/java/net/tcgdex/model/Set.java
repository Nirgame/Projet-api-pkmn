package net.tcgdex.model;

import net.tcgdex.util.CardNameUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Représente un ensemble (Set) de cartes Pokémon
 */
public class Set {
    private static final List<String> LANGUAGE_ORDER = List.of("en", "fr", "ja", "ko", "zh-cn", "zh-tw");

    private String id;
    private String name;
    private String englishName;
    private String frenchName;
    private Serie serie;
    private String logo;
    private String symbol;
    private String total;
    private CardCount cardCount;
    private String releaseDate;
    private List<CardBrief> cards;
    private List<String> availableLanguages = new ArrayList<>();
    private Map<String, String> localizedNames = new LinkedHashMap<>();

    public Set() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return getDisplayName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnglishName() {
        return englishName != null && !englishName.isBlank() ? englishName : name;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
        if (this.name == null || this.name.isBlank()) {
            this.name = englishName;
        }
    }

    public String getFrenchName() {
        return frenchName;
    }

    public void setFrenchName(String frenchName) {
        this.frenchName = frenchName;
    }

    public boolean hasFrenchName() {
        return frenchName != null && !frenchName.isBlank();
    }

    public boolean hasEnglishName() {
        return englishName != null && !englishName.isBlank();
    }

    public boolean hasDifferentFrenchName() {
        return hasFrenchName() && !getEnglishName().equalsIgnoreCase(frenchName);
    }

    public String getDisplayName() {
        if (frenchName != null && !frenchName.isBlank()) {
            return frenchName;
        }
        if (englishName != null && !englishName.isBlank()) {
            return englishName;
        }
        if (name != null && !name.isBlank()) {
            return name;
        }
        return id;
    }

    public String getSecondaryName() {
        String displayName = getDisplayName();

        if (englishName != null && !englishName.isBlank() && !englishName.equalsIgnoreCase(displayName)) {
            return englishName;
        }

        if (frenchName != null && !frenchName.isBlank() && !frenchName.equalsIgnoreCase(displayName)) {
            return frenchName;
        }

        return null;
    }

    public boolean hasSecondaryName() {
        return getSecondaryName() != null;
    }

    public boolean hasWesternTranslation() {
        return hasEnglishName() || hasFrenchName();
    }

    public String getTranslationNotice() {
        if (hasWesternTranslation()) {
            return null;
        }
        return "Pas de nom EN/FR dans TCGdex";
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean hasLogo() {
        return logo != null && !logo.isBlank();
    }

    public String getDisplayLogo() {
        if (!hasLogo()) {
            return null;
        }
        if (logo.endsWith(".png") || logo.endsWith(".jpg") || logo.endsWith(".jpeg") || logo.endsWith(".webp")) {
            return logo;
        }
        return logo + ".webp";
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean hasSymbol() {
        return symbol != null && !symbol.isBlank();
    }

    public boolean hasVisualAsset() {
        return hasLogo() || hasSymbol();
    }

    public String getDisplaySymbol() {
        if (!hasSymbol()) {
            return null;
        }
        if (symbol.endsWith(".png") || symbol.endsWith(".jpg") || symbol.endsWith(".jpeg") || symbol.endsWith(".webp")) {
            return symbol;
        }
        return symbol + ".webp";
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public CardCount getCardCount() {
        return cardCount;
    }

    public void setCardCount(CardCount cardCount) {
        this.cardCount = cardCount;
    }

    public String getDisplayTotal() {
        if (total != null && !total.isBlank()) {
            return total;
        }
        if (cardCount != null && cardCount.getTotal() != null) {
            return String.valueOf(cardCount.getTotal());
        }
        return "0";
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<CardBrief> getCards() {
        return cards;
    }

    public void setCards(List<CardBrief> cards) {
        this.cards = cards;
    }

    public List<String> getAvailableLanguages() {
        return availableLanguages;
    }

    public void setAvailableLanguages(List<String> availableLanguages) {
        if (availableLanguages == null) {
            this.availableLanguages = new ArrayList<>();
            return;
        }

        LinkedHashSet<String> orderedLanguages = new LinkedHashSet<>(availableLanguages);
        this.availableLanguages = LANGUAGE_ORDER.stream()
                .filter(orderedLanguages::contains)
                .toList();
    }

    public void addAvailableLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return;
        }

        LinkedHashSet<String> orderedLanguages = new LinkedHashSet<>(availableLanguages);
        orderedLanguages.add(languageCode);
        setAvailableLanguages(new ArrayList<>(orderedLanguages));
    }

    public Map<String, String> getLocalizedNames() {
        return localizedNames;
    }

    public void setLocalizedNames(Map<String, String> localizedNames) {
        this.localizedNames = new LinkedHashMap<>();
        if (localizedNames == null || localizedNames.isEmpty()) {
            return;
        }

        for (String languageCode : LANGUAGE_ORDER) {
            String localizedName = localizedNames.get(languageCode);
            if (localizedName != null && !localizedName.isBlank()) {
                this.localizedNames.put(languageCode, localizedName);
            }
        }
    }

    public void setLocalizedName(String languageCode, String localizedName) {
        if (languageCode == null || languageCode.isBlank() || localizedName == null || localizedName.isBlank()) {
            return;
        }

        LinkedHashMap<String, String> orderedNames = new LinkedHashMap<>(localizedNames);
        orderedNames.put(languageCode, localizedName);
        setLocalizedNames(orderedNames);
    }

    public List<String> getLocalizedAliases() {
        String displayName = CardNameUtils.normalizeForSearch(getDisplayName());
        String secondaryName = CardNameUtils.normalizeForSearch(getSecondaryName());

        return localizedNames.values().stream()
                .filter(name -> name != null && !name.isBlank())
                .filter(name -> {
                    String normalized = CardNameUtils.normalizeForSearch(name);
                    return !normalized.equals(displayName) && !normalized.equals(secondaryName);
                })
                .distinct()
                .toList();
    }

    public boolean matchesSearch(String search) {
        String normalizedSearch = CardNameUtils.normalizeForSearch(search);
        if (normalizedSearch.isBlank()) {
            return true;
        }

        String haystack = String.join(" ",
                CardNameUtils.normalizeForSearch(id),
                CardNameUtils.normalizeForSearch(name),
                CardNameUtils.normalizeForSearch(englishName),
                CardNameUtils.normalizeForSearch(frenchName),
                localizedNames.values().stream()
                        .map(CardNameUtils::normalizeForSearch)
                        .reduce("", (left, right) -> left + " " + right));

        return Arrays.stream(normalizedSearch.split(" "))
                .filter(token -> !token.isBlank())
                .allMatch(haystack::contains);
    }

    public boolean isAvailableIn(String languageCode) {
        return availableLanguages != null && availableLanguages.contains(languageCode);
    }

    public List<String> getOrderedAvailableLanguages() {
        return availableLanguages == null ? List.of() : availableLanguages;
    }

    public String getLanguageDisplayLabel(String languageCode) {
        return switch (languageCode) {
            case "en" -> "EN";
            case "fr" -> "FR";
            case "ja" -> "JP";
            case "ko" -> "KR";
            case "zh-cn" -> "ZH-CN";
            case "zh-tw" -> "ZH-TW";
            default -> languageCode == null ? "" : languageCode.toUpperCase();
        };
    }

    public String getAvailabilitySummaryLabel() {
        boolean en = isAvailableIn("en");
        boolean fr = isAvailableIn("fr");
        boolean ja = isAvailableIn("ja");
        boolean ko = isAvailableIn("ko");
        boolean zhCn = isAvailableIn("zh-cn");
        boolean zhTw = isAvailableIn("zh-tw");

        if (!en && !fr) {
            if (ja && !ko && !zhCn && !zhTw) {
                return "Exclu Japon";
            }
            if (ko && !ja && !zhCn && !zhTw) {
                return "Exclu Coree";
            }
            if ((zhCn || zhTw) && !ja && !ko) {
                if (zhCn && zhTw) {
                    return "Exclu chinois";
                }
                return zhCn ? "Exclu Chine" : "Exclu Taiwan";
            }
            return "Hors EN/FR";
        }

        if (en && fr) {
            return "EN + FR";
        }
        if (en) {
            return "EN";
        }
        if (fr) {
            return "FR";
        }

        return "Multilingue";
    }

    public String getAvailabilitySummaryClass() {
        String label = getAvailabilitySummaryLabel();
        if ("Exclu Japon".equals(label) || "Exclu Coree".equals(label) || label.startsWith("Exclu")) {
            return "bg-warning text-dark";
        }
        if ("Hors EN/FR".equals(label)) {
            return "bg-danger";
        }
        if ("EN + FR".equals(label)) {
            return "bg-success";
        }
        return "bg-secondary";
    }

    public boolean isRegionalExclusive() {
        return !isAvailableIn("en") && !isAvailableIn("fr");
    }

    public boolean isMcdoSet() {
        String displayName = getDisplayName();
        return displayName != null && displayName.toLowerCase().contains("mcdonald");
    }

    public boolean isPromoSet() {
        String displayName = getDisplayName();
        String fallbackName = name != null ? name : "";
        return displayName != null && displayName.toLowerCase().contains("promo")
                || fallbackName.toLowerCase().contains("promo");
    }

    public boolean isPocketSet() {
        if (id == null || id.isBlank()) {
            return false;
        }

        return id.equalsIgnoreCase("P-A")
                || id.matches("^[AB]\\d.*");
    }

    public boolean isAvailableInAny(List<String> languageCodes) {
        if (languageCodes == null || languageCodes.isEmpty()) {
            return true;
        }

        return languageCodes.stream().anyMatch(this::isAvailableIn);
    }

    @Override
    public String toString() {
        return "Set{" +
                "id='" + id + '\'' +
                ", displayName='" + getDisplayName() + '\'' +
                ", frenchName='" + frenchName + '\'' +
                ", serie='" + serie + '\'' +
                ", total='" + getDisplayTotal() + '\'' +
                ", releaseDate=" + releaseDate +
                '}';
    }
}
