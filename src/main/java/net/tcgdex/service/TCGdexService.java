package net.tcgdex.service;

import net.tcgdex.TCGdexClient;
import net.tcgdex.util.HttpClientUtil;
import net.tcgdex.model.Card;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.Set;
import net.tcgdex.model.Serie;
import net.tcgdex.util.CardNameUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TCGdexService {
    private static final List<String> LANGUAGE_PRIORITY = List.of("en", "fr", "ja", "ko", "zh-cn", "zh-tw");

    private final TCGdexClient englishClient;
    private final TCGdexClient frenchClient;
    private final Map<String, TCGdexClient> clientsByLanguage;
    private final Map<String, Card> cardCache = new ConcurrentHashMap<>();
    private final Map<String, List<Set>> setListCache = new ConcurrentHashMap<>();
    private final Map<String, List<CardBrief>> cardSearchCache = new ConcurrentHashMap<>();
    private final Map<String, List<CardBrief>> cardRaritySearchCache = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    private volatile List<CardBrief> mergedCardsCache;
    private volatile List<String> frenchRarityOptionsCache;

    public TCGdexService() {
        this.englishClient = new TCGdexClient("en");
        this.frenchClient = new TCGdexClient("fr");
        this.clientsByLanguage = Map.of(
                "en", englishClient,
                "fr", frenchClient,
                "ja", new TCGdexClient("ja"),
                "ko", new TCGdexClient("ko"),
                "zh-cn", new TCGdexClient("zh-cn"),
                "zh-tw", new TCGdexClient("zh-tw"));
    }

    public List<Serie> getSeries() throws IOException {
        return englishClient.getSerieService().listSeries();
    }

    public List<Set> getSets() throws IOException {
        LinkedHashMap<String, Set> setsById = new LinkedHashMap<>();

        for (String language : LANGUAGE_PRIORITY) {
            for (Set localizedSet : getSetsForLanguage(language)) {
                if (localizedSet.getId() == null || localizedSet.getId().isBlank()) {
                    continue;
                }

                Set mergedSet = setsById.computeIfAbsent(localizedSet.getId(), ignored -> copySet(localizedSet));
                mergeLocalizedSet(mergedSet, localizedSet, language);
            }
        }

        return new ArrayList<>(setsById.values());
    }

    public List<CardBrief> getCards() throws IOException {
        List<CardBrief> cachedCards = mergedCardsCache;
        if (cachedCards != null) {
            return cachedCards;
        }

        synchronized (this) {
            if (mergedCardsCache != null) {
                return mergedCardsCache;
            }

            List<CardBrief> englishCards = englishClient.getCardService().listCards("");
            List<CardBrief> frenchCards = frenchClient.getCardService().listCards("");
            mergedCardsCache = mergeCardBriefs(englishCards, frenchCards);
            return mergedCardsCache;
        }
    }

    public List<CardBrief> getCardsBySet(String setId) throws IOException {
        Set mergedSet = getSet(setId);
        List<CardBrief> merged = mergedSet.getCards() != null ? mergedSet.getCards() : Collections.emptyList();
        if (!merged.isEmpty()) {
            return merged;
        }

        String prefix = setId.toLowerCase() + "-";
        return getCards().stream()
                .filter(card -> card.getId() != null && card.getId().toLowerCase().startsWith(prefix))
                .toList();
    }

    public List<CardBrief> searchCards(String query) throws IOException {
        String normalizedQuery = CardNameUtils.normalizeForSearch(query);
        if (normalizedQuery.isBlank()) {
            return getCards();
        }

        List<CardBrief> cachedResults = cardSearchCache.get(normalizedQuery);
        if (cachedResults != null) {
            return cachedResults;
        }

        List<CardBrief> results = getCards().stream()
                .filter(card -> CardNameUtils.matchesSearch(card, query))
                .toList();
        cardSearchCache.put(normalizedQuery, results);
        return results;
    }

    public List<CardBrief> filterCardsForSearch(List<CardBrief> cards, String query) throws IOException {
        return cards.stream()
                .filter(card -> CardNameUtils.matchesSearch(card, query))
                .toList();
    }

    public List<String> getAvailableRarities() throws IOException {
        List<String> cachedRarities = frenchRarityOptionsCache;
        if (cachedRarities != null) {
            return cachedRarities;
        }

        synchronized (this) {
            if (frenchRarityOptionsCache != null) {
                return frenchRarityOptionsCache;
            }

            String response = HttpClientUtil.get("/fr/rarities");
            List<String> rarities = gson.fromJson(response, new TypeToken<List<String>>() {
            }.getType());
            frenchRarityOptionsCache = rarities == null ? List.of() : rarities.stream()
                    .filter(rarity -> rarity != null && !rarity.isBlank())
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            return frenchRarityOptionsCache;
        }
    }

    public List<CardBrief> getCardsByRarity(String frenchRarity) throws IOException {
        if (frenchRarity == null || frenchRarity.isBlank()) {
            return getCards();
        }

        List<CardBrief> cachedResults = cardRaritySearchCache.get(frenchRarity);
        if (cachedResults != null) {
            return cachedResults;
        }

        List<CardBrief> frenchCards = frenchClient.getCardService().listCards("rarity=" + frenchRarity);
        Map<String, CardBrief> allCardsById = getCards().stream()
                .collect(Collectors.toMap(CardBrief::getId, card -> card, (left, right) -> left, LinkedHashMap::new));

        List<CardBrief> mergedResults = frenchCards.stream()
                .map(card -> allCardsById.get(card.getId()))
                .filter(card -> card != null)
                .toList();

        cardRaritySearchCache.put(frenchRarity, mergedResults);
        return mergedResults;
    }

    public Card getCard(String cardId) throws IOException {
        Card cachedCard = cardCache.get(cardId);
        if (cachedCard != null) {
            return copyCard(cachedCard);
        }

        Card englishCard = englishClient.getCardService().getCard(cardId);
        Card frenchCard = tryGetCard("fr", cardId);

        englishCard.setEnglishName(englishCard.getName());
        if (frenchCard != null) {
            englishCard.setFrenchName(frenchCard.getName());
            englishCard.setFrenchRarity(frenchCard.getRarity());
        }
        englishCard.setEnglishRarity(englishCard.getRarity());
        englishCard.setFormLabel(CardNameUtils.inferFormLabel(englishCard));

        cardCache.put(cardId, copyCard(englishCard));
        return englishCard;
    }

    public Set getSet(String setId) throws IOException {
        LinkedHashMap<String, Set> localizedSets = new LinkedHashMap<>();

        for (String language : LANGUAGE_PRIORITY) {
            Set localizedSet = tryGetSet(language, setId);
            if (localizedSet != null) {
                localizedSets.put(language, localizedSet);
            }
        }

        if (localizedSets.isEmpty()) {
            throw new IOException("Set not found: " + setId);
        }

        Set mergedSet = null;
        for (Map.Entry<String, Set> entry : localizedSets.entrySet()) {
            if (mergedSet == null) {
                mergedSet = copySet(entry.getValue());
            }
            mergeLocalizedSet(mergedSet, entry.getValue(), entry.getKey());
        }

        if (mergedSet != null) {
            mergedSet.setCards(mergeLocalizedCards(localizedSets));
        }

        return mergedSet;
    }

    public Serie getSerie(String serieId) throws IOException {
        return englishClient.getSerieService().getSerie(serieId);
    }

    public List<Set> getSetsBySerie(String serieId) throws IOException {
        Serie serie = getSerie(serieId);
        if (serie.getSets() == null || serie.getSets().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Set> allSetsById = getSets().stream()
                .collect(Collectors.toMap(Set::getId, set -> set, (left, right) -> left, LinkedHashMap::new));

        Serie serieReference = new Serie(serie.getId(), serie.getName(), serie.getLogo());
        return serie.getSets().stream()
                .map(serieSet -> {
                    Set mergedSet = allSetsById.get(serieSet.getId());
                    if (mergedSet != null) {
                        mergedSet.setSerie(serieReference);
                        return mergedSet;
                    }

                    Set fallbackSet = copySet(serieSet);
                    fallbackSet.setSerie(serieReference);
                    return fallbackSet;
                })
                .toList();
    }

    public void enrichFormLabels(List<CardBrief> cards) throws IOException {
        for (CardBrief card : cards) {
            if (card.getFormLabel() == null || card.getFormLabel().isBlank()) {
                String namedForm = CardNameUtils.extractNamedFormLabel(card.getEnglishName(), card.getFrenchName());
                if (namedForm != null) {
                    card.setFormLabel(namedForm);
                }
            }
        }
    }

    public void enrichRarities(List<CardBrief> cards) throws IOException {
        for (CardBrief card : cards) {
            if (card.getDisplayRarity() != null && !card.getDisplayRarity().isBlank()) {
                continue;
            }

            try {
                Card detailedCard = getCard(card.getId());
                card.setEnglishRarity(detailedCard.getEnglishRarity());
                card.setFrenchRarity(detailedCard.getFrenchRarity());
            } catch (IOException ignored) {
                // Best-effort enrichment: a missing localized detail must not block the whole page.
            }
        }
    }

    private List<CardBrief> mergeCardBriefs(List<CardBrief> englishCards, List<CardBrief> frenchCards) {
        Map<String, CardBrief> frenchById = frenchCards.stream()
                .filter(card -> card.getId() != null)
                .collect(Collectors.toMap(CardBrief::getId, card -> card, (left, right) -> left, LinkedHashMap::new));

        return englishCards.stream()
                .map(englishCard -> {
                    englishCard.setEnglishName(englishCard.getName());
                    CardBrief frenchCard = frenchById.get(englishCard.getId());
                    if (frenchCard != null) {
                        englishCard.setFrenchName(frenchCard.getName());
                    }
                    Card cachedCard = cardCache.get(englishCard.getId());
                    if (cachedCard != null) {
                        englishCard.setEnglishRarity(cachedCard.getEnglishRarity());
                        englishCard.setFrenchRarity(cachedCard.getFrenchRarity());
                    }
                    englishCard.setFormLabel(CardNameUtils.extractNamedFormLabel(
                            englishCard.getEnglishName(),
                            englishCard.getFrenchName()));
                    return englishCard;
                })
                .toList();
    }

    private List<Set> getSetsForLanguage(String language) throws IOException {
        List<Set> cachedSets = setListCache.get(language);
        if (cachedSets != null) {
            return cachedSets;
        }

        TCGdexClient client = clientsByLanguage.get(language);
        if (client == null) {
            return Collections.emptyList();
        }

        List<Set> fetchedSets = client.getSetService().listSets();
        setListCache.put(language, fetchedSets);
        return fetchedSets;
    }

    private Set tryGetSet(String language, String setId) throws IOException {
        TCGdexClient client = clientsByLanguage.get(language);
        if (client == null) {
            return null;
        }

        try {
            return client.getSetService().getSet(setId);
        } catch (IOException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("404")) {
                return null;
            }
            throw exception;
        }
    }

    private Card tryGetCard(String language, String cardId) throws IOException {
        TCGdexClient client = clientsByLanguage.get(language);
        if (client == null) {
            return null;
        }

        try {
            return client.getCardService().getCard(cardId);
        } catch (IOException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("404")) {
                return null;
            }
            throw exception;
        }
    }

    private void mergeLocalizedSet(Set mergedSet, Set localizedSet, String language) {
        if (mergedSet.getName() == null || mergedSet.getName().isBlank()) {
            mergedSet.setName(localizedSet.getName());
        }

        if ("en".equals(language)) {
            mergedSet.setEnglishName(localizedSet.getName());
        } else if ("fr".equals(language)) {
            mergedSet.setFrenchName(localizedSet.getName());
        }

        if ((mergedSet.getLogo() == null || mergedSet.getLogo().isBlank()) && localizedSet.getLogo() != null) {
            mergedSet.setLogo(localizedSet.getLogo());
        }

        if ((mergedSet.getSymbol() == null || mergedSet.getSymbol().isBlank()) && localizedSet.getSymbol() != null) {
            mergedSet.setSymbol(localizedSet.getSymbol());
        }

        if ((mergedSet.getTotal() == null || mergedSet.getTotal().isBlank()) && localizedSet.getTotal() != null) {
            mergedSet.setTotal(localizedSet.getTotal());
        }

        if (mergedSet.getCardCount() == null && localizedSet.getCardCount() != null) {
            mergedSet.setCardCount(localizedSet.getCardCount());
        }

        if ((mergedSet.getReleaseDate() == null || mergedSet.getReleaseDate().isBlank()) && localizedSet.getReleaseDate() != null) {
            mergedSet.setReleaseDate(localizedSet.getReleaseDate());
        }

        if (mergedSet.getSerie() == null && localizedSet.getSerie() != null) {
            mergedSet.setSerie(localizedSet.getSerie());
        }

        mergedSet.addAvailableLanguage(language);
    }

    private List<CardBrief> mergeLocalizedCards(Map<String, Set> localizedSets) {
        LinkedHashMap<String, CardBrief> cardsById = new LinkedHashMap<>();

        for (String language : LANGUAGE_PRIORITY) {
            Set localizedSet = localizedSets.get(language);
            if (localizedSet == null || localizedSet.getCards() == null) {
                continue;
            }

            for (CardBrief localizedCard : localizedSet.getCards()) {
                if (localizedCard.getId() == null || localizedCard.getId().isBlank()) {
                    continue;
                }

                CardBrief mergedCard = cardsById.computeIfAbsent(localizedCard.getId(), ignored -> copyCardBrief(localizedCard));

                if (("en".equals(language) || mergedCard.getName() == null || mergedCard.getName().isBlank())
                        && localizedCard.getName() != null) {
                    mergedCard.setName(localizedCard.getName());
                }

                if ("en".equals(language)) {
                    mergedCard.setEnglishName(localizedCard.getName());
                } else if ("fr".equals(language)) {
                    mergedCard.setFrenchName(localizedCard.getName());
                }

                if ((mergedCard.getImage() == null || mergedCard.getImage().isBlank()) && localizedCard.getImage() != null) {
                    mergedCard.setImage(localizedCard.getImage());
                }

                if ((mergedCard.getLocalId() == null || mergedCard.getLocalId().isBlank()) && localizedCard.getLocalId() != null) {
                    mergedCard.setLocalId(localizedCard.getLocalId());
                }

                Card cachedCard = cardCache.get(mergedCard.getId());
                if (cachedCard != null) {
                    mergedCard.setEnglishRarity(cachedCard.getEnglishRarity());
                    mergedCard.setFrenchRarity(cachedCard.getFrenchRarity());
                }
            }
        }

        return new ArrayList<>(cardsById.values());
    }

    private Set copySet(Set source) {
        Set copy = new Set();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setEnglishName(source.getEnglishName());
        copy.setFrenchName(source.getFrenchName());
        copy.setSerie(source.getSerie());
        copy.setLogo(source.getLogo());
        copy.setSymbol(source.getSymbol());
        copy.setTotal(source.getTotal());
        copy.setCardCount(source.getCardCount());
        copy.setReleaseDate(source.getReleaseDate());
        copy.setCards(source.getCards());
        copy.setAvailableLanguages(source.getAvailableLanguages());
        return copy;
    }

    private CardBrief copyCardBrief(CardBrief source) {
        CardBrief copy = new CardBrief();
        copy.setId(source.getId());
        copy.setLocalId(source.getLocalId());
        copy.setName(source.getName());
        copy.setEnglishName(source.getEnglishName());
        copy.setFrenchName(source.getFrenchName());
        copy.setEnglishRarity(source.getEnglishRarity());
        copy.setFrenchRarity(source.getFrenchRarity());
        copy.setFormLabel(source.getFormLabel());
        copy.setImage(source.getImage());
        return copy;
    }

    private Card copyCard(Card source) {
        Card copy = new Card();
        copy.setId(source.getId());
        copy.setLocalId(source.getLocalId());
        copy.setName(source.getName());
        copy.setEnglishName(source.getEnglishName());
        copy.setFrenchName(source.getFrenchName());
        copy.setFormLabel(source.getFormLabel());
        copy.setImage(source.getImage());
        copy.setCategory(source.getCategory());
        copy.setIllustrator(source.getIllustrator());
        copy.setRarity(source.getRarity());
        copy.setCardType(source.getCardType());
        copy.setVariants(source.getVariants());
        copy.setDexId(source.getDexId());
        copy.setTypes(source.getTypes());
        copy.setStage(source.getStage());
        copy.setEvolveFrom(source.getEvolveFrom());
        copy.setAbilities(source.getAbilities());
        copy.setAttacks(source.getAttacks());
        return copy;
    }
}
