package net.tcgdex.service;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.entity.UserPokemonCardAssignment;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.PokedexDetailView;
import net.tcgdex.model.PokedexListItem;
import net.tcgdex.model.PokedexPageResult;
import net.tcgdex.model.PokemonIndexEntry;
import net.tcgdex.model.PokemonSpeciesInfo;
import net.tcgdex.model.RegionalDisplayMode;
import net.tcgdex.model.RegionalForm;
import net.tcgdex.repository.UserCardRepository;
import net.tcgdex.repository.UserPokemonCardAssignmentRepository;
import net.tcgdex.util.CardNameUtils;
import net.tcgdex.util.PokemonNameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class PokedexService {

    private final PokeApiService pokeApiService;
    private final TCGdexService tcgdexService;
    private final CollectionService collectionService;
    private final UserCardRepository userCardRepository;
    private final UserPokemonCardAssignmentRepository assignmentRepository;
    private final Map<Integer, List<CardBrief>> availableCardsCache = new ConcurrentHashMap<>();

    public PokedexService(PokeApiService pokeApiService,
            TCGdexService tcgdexService,
            CollectionService collectionService,
            UserCardRepository userCardRepository,
            UserPokemonCardAssignmentRepository assignmentRepository) {
        this.pokeApiService = pokeApiService;
        this.tcgdexService = tcgdexService;
        this.collectionService = collectionService;
        this.userCardRepository = userCardRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public PokedexPageResult getPokedexPage(User user,
            String search,
            Integer generation,
            boolean assignedOnly,
            boolean unassignedOnly,
            boolean missingCardOnly,
            RegionalDisplayMode regionalMode,
            RegionalForm regionalForm,
            RegionalDisplayMode megaGigantamaxMode,
            int page,
            int size) throws IOException {
        List<UserPokemonCardAssignment> assignments = assignmentRepository.findByUser(user);
        Map<Integer, UserCard> assignedCardsByPokemon = getAssignedCardsByPokemon(user, assignments);
        Set<Integer> missingCardPokemonIds = getMissingCardPokemonIds(assignments);
        Map<Integer, String> commentsByPokemon = getCommentsByPokemon(assignments);
        List<PokemonIndexEntry> filteredEntries = filterEntries(
                search,
                generation,
                assignedOnly,
                unassignedOnly,
                missingCardOnly,
                regionalMode,
                regionalForm,
                megaGigantamaxMode,
                assignedCardsByPokemon.keySet(),
                missingCardPokemonIds);
        Set<Integer> allAssignedPokemonIds = new LinkedHashSet<>(assignedCardsByPokemon.keySet());
        allAssignedPokemonIds.addAll(missingCardPokemonIds);
        int unassignedCount = (int) filteredEntries.stream()
                .filter(entry -> !allAssignedPokemonIds.contains(entry.id()))
                .count();

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int start = (safePage - 1) * safeSize;
        List<PokemonIndexEntry> currentPageEntries = start >= filteredEntries.size()
                ? List.of()
                : filteredEntries.subList(start, Math.min(start + safeSize, filteredEntries.size()));

        List<UserCard> userCollection = collectionService.getUserCollection(user);
        List<PokedexListItem> pokemons = currentPageEntries.stream()
                .map(entry -> toListItem(entry, assignedCardsByPokemon, missingCardPokemonIds, commentsByPokemon, userCollection))
                .toList();

        return new PokedexPageResult(
                pokemons,
                pokeApiService.getGenerationOptions(),
                List.of(RegionalForm.values()),
                safePage,
                Math.max(1, (int) Math.ceil((double) filteredEntries.size() / safeSize)),
                filteredEntries.size(),
                unassignedCount);
    }

    public PokedexDetailView getPokemonDetail(User user, int pokemonId) throws IOException {
        return getPokemonDetail(
                user,
                pokemonId,
                null,
                null,
                false,
                false,
                false,
                RegionalDisplayMode.INCLUDE,
                null,
                RegionalDisplayMode.INCLUDE);
    }

    public PokedexDetailView getPokemonDetail(User user,
            int pokemonId,
            String search,
            Integer generation,
            boolean assignedOnly,
            boolean unassignedOnly,
            boolean missingCardOnly,
            RegionalDisplayMode regionalMode,
            RegionalForm regionalForm,
            RegionalDisplayMode megaGigantamaxMode) throws IOException {
        PokemonSpeciesInfo species = pokeApiService.getPokemonSpecies(pokemonId);
        List<UserPokemonCardAssignment> assignments = assignmentRepository.findByUser(user);
        Map<Integer, UserCard> assignedCardsByPokemon = getAssignedCardsByPokemon(user, assignments);
        Set<Integer> missingCardPokemonIds = getMissingCardPokemonIds(assignments);
        Optional<UserPokemonCardAssignment> assignment = assignments.stream()
                .filter(entry -> entry.getPokemonId() == pokemonId)
                .findFirst();
        boolean missingCardMarked = assignment.map(UserPokemonCardAssignment::isCardMissing).orElse(false);
        String comment = assignment.map(UserPokemonCardAssignment::getComment).map(this::normalizeComment).orElse(null);
        UserCard assignedCard = getAssignedCard(user, pokemonId).orElse(null);
        List<UserCard> userCollection = collectionService.getUserCollection(user);

        List<UserCard> ownedCards = userCollection.stream()
                .filter(card -> matchesDisplayPool(cardToBrief(card), species))
                .sorted(Comparator.comparing(UserCard::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<String, Integer> ownedCardCounts = ownedCards.stream()
                .collect(Collectors.toMap(UserCard::getCardId, UserCard::getQuantity, Integer::sum, LinkedHashMap::new));

        List<CardBrief> availableCards = getCardsForPokemon(species).stream()
                .sorted(Comparator.comparing(CardBrief::getEnglishName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        tcgdexService.enrichRarities(availableCards);
        tcgdexService.enrichSetMetadata(availableCards);
        List<PokemonIndexEntry> filteredEntries = filterEntries(
                search,
                generation,
                assignedOnly,
                unassignedOnly,
                missingCardOnly,
                regionalMode,
                regionalForm,
                megaGigantamaxMode,
                assignedCardsByPokemon.keySet(),
                missingCardPokemonIds);
        PokemonSpeciesInfo[] adjacentSpecies = getAdjacentSpecies(pokemonId, filteredEntries);

        return new PokedexDetailView(
                species,
                assignedCard,
                ownedCards,
                availableCards,
                ownedCardCounts,
                missingCardMarked,
                comment,
                adjacentSpecies[0],
                adjacentSpecies[1]);
    }

    public UserPokemonCardAssignment assignCard(User user, int pokemonId, String cardId) throws IOException {
        return assignCard(user, pokemonId, cardId, null);
    }

    public UserPokemonCardAssignment assignCard(User user, int pokemonId, String cardId, String comment) throws IOException {
        pokeApiService.getPokemonSpecies(pokemonId);
        UserCard userCard = userCardRepository.findByUserAndCardId(user, cardId)
                .orElseThrow(() -> new IllegalArgumentException("Cette carte n'est pas encore dans votre collection."));

        UserPokemonCardAssignment assignment = assignmentRepository.findByUserAndPokemonId(user, pokemonId)
                .orElseGet(UserPokemonCardAssignment::new);
        assignment.setUser(user);
        assignment.setPokemonId(pokemonId);
        assignment.setAssignedCardId(userCard.getCardId());
        assignment.setCardMissing(false);
        assignment.setComment(normalizeComment(comment));
        return assignmentRepository.save(assignment);
    }

    public UserPokemonCardAssignment markMissingCard(User user, int pokemonId) throws IOException {
        return markMissingCard(user, pokemonId, null);
    }

    public UserPokemonCardAssignment markMissingCard(User user, int pokemonId, String comment) throws IOException {
        pokeApiService.getPokemonSpecies(pokemonId);
        UserPokemonCardAssignment assignment = assignmentRepository.findByUserAndPokemonId(user, pokemonId)
                .orElseGet(UserPokemonCardAssignment::new);
        assignment.setUser(user);
        assignment.setPokemonId(pokemonId);
        assignment.setAssignedCardId(null);
        assignment.setCardMissing(true);
        assignment.setComment(normalizeComment(comment));
        return assignmentRepository.save(assignment);
    }

    public UserPokemonCardAssignment saveComment(User user, int pokemonId, String comment) throws IOException {
        pokeApiService.getPokemonSpecies(pokemonId);
        String normalizedComment = normalizeComment(comment);
        Optional<UserPokemonCardAssignment> existingAssignment = assignmentRepository.findByUserAndPokemonId(user, pokemonId);

        if (existingAssignment.isEmpty() && normalizedComment == null) {
            return null;
        }

        UserPokemonCardAssignment assignment = existingAssignment.orElseGet(UserPokemonCardAssignment::new);
        assignment.setUser(user);
        assignment.setPokemonId(pokemonId);
        assignment.setComment(normalizedComment);

        if (assignment.getAssignedCardId() == null && !assignment.isCardMissing() && normalizedComment == null) {
            if (assignment.getId() != null) {
                assignmentRepository.delete(assignment);
            }
            return null;
        }

        return assignmentRepository.save(assignment);
    }

    public void clearAssignment(User user, int pokemonId) {
        assignmentRepository.findByUserAndPokemonId(user, pokemonId)
                .ifPresent(assignment -> {
                    assignment.setAssignedCardId(null);
                    assignment.setCardMissing(false);
                    assignment.setComment(normalizeComment(assignment.getComment()));
                    if (assignment.getComment() == null) {
                        assignmentRepository.delete(assignment);
                    } else {
                        assignmentRepository.save(assignment);
                    }
                });
    }

    public Optional<UserCard> getAssignedCard(User user, int pokemonId) {
        Optional<UserPokemonCardAssignment> assignment = getAssignment(user, pokemonId);
        if (assignment.isEmpty()) {
            return Optional.empty();
        }
        if (assignment.get().isCardMissing() || assignment.get().getAssignedCardId() == null || assignment.get().getAssignedCardId().isBlank()) {
            return Optional.empty();
        }

        Optional<UserCard> userCard = userCardRepository.findByUserAndCardId(user, assignment.get().getAssignedCardId());
        if (userCard.isEmpty()) {
            if (!assignment.get().isCardMissing()) {
                assignmentRepository.delete(assignment.get());
            }
            return Optional.empty();
        }

        return userCard;
    }

    private PokedexListItem toListItem(PokemonIndexEntry entry,
            Map<Integer, UserCard> assignedCardsByPokemon,
            Set<Integer> missingCardPokemonIds,
            Map<Integer, String> commentsByPokemon,
            List<UserCard> userCollection) {
        try {
            PokemonSpeciesInfo species = pokeApiService.getPokemonSpecies(entry.id());
            UserCard assignedCard = assignedCardsByPokemon.get(entry.id());
            long ownedCardCount = countOwnedCardsForSpecies(userCollection, species);
            return new PokedexListItem(
                    species,
                    assignedCard,
                    ownedCardCount,
                    missingCardPokemonIds.contains(entry.id()),
                    commentsByPokemon.get(entry.id()));
        } catch (IOException exception) {
            PokemonSpeciesInfo fallbackSpecies = new PokemonSpeciesInfo(
                    entry.id(),
                    entry.speciesId(),
                    entry.slug(),
                    PokemonNameUtils.slugToDisplayName(entry.slug()),
                    null,
                    entry.generationId(),
                    entry.generationLabel(),
                    entry.regionalForm());
            return new PokedexListItem(
                    fallbackSpecies,
                    assignedCardsByPokemon.get(entry.id()),
                    0,
                    missingCardPokemonIds.contains(entry.id()),
                    commentsByPokemon.get(entry.id()));
        }
    }

    private List<PokemonIndexEntry> filterEntries(String search,
            Integer generation,
            boolean assignedOnly,
            boolean unassignedOnly,
            boolean missingCardOnly,
            RegionalDisplayMode regionalMode,
            RegionalForm regionalForm,
            RegionalDisplayMode megaGigantamaxMode,
            Set<Integer> assignedPokemonIds,
            Set<Integer> missingCardPokemonIds) throws IOException {
        Set<Integer> allAssignedPokemonIds = new LinkedHashSet<>(assignedPokemonIds);
        allAssignedPokemonIds.addAll(missingCardPokemonIds);

        List<PokemonIndexEntry> entries = pokeApiService.getPokedexEntries().stream()
                        .filter(entry -> switch (megaGigantamaxMode) {
                            case INCLUDE -> true;
                            case EXCLUDE -> !isMegaGigantamaxEntry(entry);
                            case ONLY -> isMegaGigantamaxEntry(entry);
                        })
                        .filter(entry -> switch (regionalMode) {
                            case INCLUDE -> true;
                            case EXCLUDE -> !isAlternativeFilterEntry(entry);
                            case ONLY -> isAlternativeViewEntry(entry);
                        })
                        .toList();

        if (assignedOnly && !unassignedOnly) {
            entries = entries.stream()
                    .filter(entry -> allAssignedPokemonIds.contains(entry.id()))
                    .toList();
        } else if (unassignedOnly && !assignedOnly) {
            entries = entries.stream()
                    .filter(entry -> !allAssignedPokemonIds.contains(entry.id()))
                    .toList();
        }

        if (missingCardOnly) {
            entries = entries.stream()
                    .filter(entry -> missingCardPokemonIds.contains(entry.id()))
                    .toList();
        }

        if (generation != null && generation > 0) {
            entries = entries.stream()
                    .filter(entry -> generation.equals(entry.generationId()))
                    .toList();
        }

        if (regionalForm != null) {
            entries = entries.stream()
                    .filter(entry -> regionalForm.equals(entry.regionalForm())
                            || (entry.alternativeForm() != null && regionalForm.equals(entry.alternativeForm().regionalForm())))
                    .toList();
        }

        if (search == null || search.isBlank()) {
            return entries;
        }

        return entries.stream()
                .filter(entry -> matchesSearchQuick(entry, search))
                .toList();
    }

    private boolean matchesSearchQuick(PokemonIndexEntry entry, String search) {
        String normalizedSearch = CardNameUtils.normalizeForSearch(search);
        if (normalizedSearch.isBlank()) {
            return true;
        }

        PokemonSpeciesInfo cachedSpecies = pokeApiService.findCachedPokemonSpecies(entry.speciesId());
        String haystack = String.join(" ",
                String.valueOf(entry.id()),
                String.valueOf(entry.speciesId()),
                CardNameUtils.normalizeForSearch(entry.slug()),
                CardNameUtils.normalizeForSearch(PokemonNameUtils.slugToDisplayName(entry.slug())),
                CardNameUtils.normalizeForSearch(entry.englishName()),
                CardNameUtils.normalizeForSearch(entry.frenchName()),
                CardNameUtils.normalizeForSearch(entry.formLabel()),
                CardNameUtils.normalizeForSearch(entry.generationLabel()),
                CardNameUtils.normalizeForSearch(entry.regionalForm() != null ? entry.regionalForm().label() : null),
                cachedSpecies == null ? "" : CardNameUtils.normalizeForSearch(cachedSpecies.englishName()),
                cachedSpecies == null ? "" : CardNameUtils.normalizeForSearch(cachedSpecies.frenchName()),
                cachedSpecies == null || entry.regionalForm() == null
                        ? ""
                        : CardNameUtils.normalizeForSearch(cachedSpecies.englishName() + " " + entry.regionalForm().englishPrefix()),
                cachedSpecies == null || entry.regionalForm() == null
                        ? ""
                        : CardNameUtils.normalizeForSearch(cachedSpecies.frenchName() + " " + entry.regionalForm().frenchSuffix()));

        return haystack.contains(normalizedSearch);
    }

    private boolean isAlternativeFilterEntry(PokemonIndexEntry entry) {
        return entry.isRegionalForm()
                || (entry.isAlternativeForm()
                && !entry.alternativeForm().replacesBaseEntry()
                && !entry.alternativeForm().isMegaOrGigantamaxForm());
    }

    private boolean isAlternativeViewEntry(PokemonIndexEntry entry) {
        return entry.isRegionalForm()
                || (entry.isAlternativeForm() && !entry.alternativeForm().isMegaOrGigantamaxForm());
    }

    private boolean isMegaGigantamaxEntry(PokemonIndexEntry entry) {
        return entry.alternativeForm() != null && entry.alternativeForm().isMegaOrGigantamaxForm();
    }

    private Map<Integer, UserCard> getAssignedCardsByPokemon(User user, List<UserPokemonCardAssignment> assignments) {
        Map<Integer, UserCard> assignedCards = new LinkedHashMap<>();
        for (UserPokemonCardAssignment assignment : assignments) {
            if (assignment.isCardMissing() || assignment.getAssignedCardId() == null || assignment.getAssignedCardId().isBlank()) {
                continue;
            }
            userCardRepository.findByUserAndCardId(user, assignment.getAssignedCardId())
                    .ifPresentOrElse(
                            card -> assignedCards.put(assignment.getPokemonId(), card),
                            () -> assignmentRepository.delete(assignment));
        }
        return assignedCards;
    }

    private Set<Integer> getMissingCardPokemonIds(List<UserPokemonCardAssignment> assignments) {
        return assignments.stream()
                .filter(UserPokemonCardAssignment::isCardMissing)
                .map(UserPokemonCardAssignment::getPokemonId)
                .collect(Collectors.toSet());
    }

    private Map<Integer, String> getCommentsByPokemon(List<UserPokemonCardAssignment> assignments) {
        return assignments.stream()
                .filter(assignment -> normalizeComment(assignment.getComment()) != null)
                .collect(Collectors.toMap(
                        UserPokemonCardAssignment::getPokemonId,
                        assignment -> normalizeComment(assignment.getComment()),
                        (left, right) -> right,
                        LinkedHashMap::new));
    }

    private Optional<UserPokemonCardAssignment> getAssignment(User user, int pokemonId) {
        return assignmentRepository.findByUserAndPokemonId(user, pokemonId);
    }

    private PokemonSpeciesInfo[] getAdjacentSpecies(int pokemonId, List<PokemonIndexEntry> entries) throws IOException {
        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index).id() != pokemonId) {
                continue;
            }

            PokemonSpeciesInfo previousSpecies = index > 0
                    ? pokeApiService.getPokemonSpecies(entries.get(index - 1).id())
                    : null;
            PokemonSpeciesInfo nextSpecies = index + 1 < entries.size()
                    ? pokeApiService.getPokemonSpecies(entries.get(index + 1).id())
                    : null;
            return new PokemonSpeciesInfo[] { previousSpecies, nextSpecies };
        }

        return getDefaultAdjacentSpecies(pokemonId);
    }

    private PokemonSpeciesInfo[] getDefaultAdjacentSpecies(int pokemonId) throws IOException {
        List<PokemonIndexEntry> entries = pokeApiService.getPokedexEntries();
        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index).id() != pokemonId) {
                continue;
            }

            PokemonSpeciesInfo previousSpecies = index > 0
                    ? pokeApiService.getPokemonSpecies(entries.get(index - 1).id())
                    : null;
            PokemonSpeciesInfo nextSpecies = index + 1 < entries.size()
                    ? pokeApiService.getPokemonSpecies(entries.get(index + 1).id())
                    : null;
            return new PokemonSpeciesInfo[] { previousSpecies, nextSpecies };
        }

        return new PokemonSpeciesInfo[] { null, null };
    }

    private List<CardBrief> getCardsForPokemon(PokemonSpeciesInfo species) throws IOException {
        List<CardBrief> cachedCards = availableCardsCache.get(species.id());
        if (cachedCards != null) {
            return cachedCards;
        }

        Map<String, CardBrief> cardsById = new LinkedHashMap<>();
        PokemonSpeciesInfo baseSpecies = pokeApiService.getBasePokemonSpecies(species.speciesId());
        Set<String> baseQueries = new LinkedHashSet<>();
        baseQueries.add(baseSpecies.englishName());
        baseQueries.add(baseSpecies.frenchName());
        baseQueries.add(species.slug());
        if (species.isAlternativeForm()) {
            collectBaseSpeciesCards(cardsById, species, baseQueries);
        } else {
            collectCardsForQueries(cardsById, species, baseQueries);
        }

        List<CardBrief> cards = cardsById.isEmpty()
                ? Collections.emptyList()
                : List.copyOf(cardsById.values());
        tcgdexService.enrichFormLabels(cards);
        availableCardsCache.put(species.id(), cards);
        return cards;
    }

    private void collectCardsForQueries(Map<String, CardBrief> cardsById,
            PokemonSpeciesInfo species,
            Set<String> rawQueries) throws IOException {
        Set<String> queries = new LinkedHashSet<>(rawQueries);
        for (String query : queries) {
            if (query == null || query.isBlank()) {
                continue;
            }

            for (CardBrief card : tcgdexService.searchCards(query)) {
                if (PokemonNameUtils.matchesSpecies(card, species)) {
                    cardsById.putIfAbsent(card.getId(), card);
                }
            }
        }
    }

    private void collectBaseSpeciesCards(Map<String, CardBrief> cardsById,
            PokemonSpeciesInfo species,
            Set<String> rawQueries) throws IOException {
        Set<String> queries = new LinkedHashSet<>(rawQueries);
        for (String query : queries) {
            if (query == null || query.isBlank()) {
                continue;
            }

            for (CardBrief card : tcgdexService.searchCards(query)) {
                if (PokemonNameUtils.matchesBaseSpecies(card, species)) {
                    cardsById.putIfAbsent(card.getId(), card);
                }
            }
        }
    }

    private long countOwnedCardsForSpecies(List<UserCard> userCollection, PokemonSpeciesInfo species) {
        return userCollection.stream()
                .filter(card -> matchesDisplayPool(cardToBrief(card), species))
                .mapToLong(UserCard::getQuantity)
                .sum();
    }

    private boolean matchesDisplayPool(CardBrief card, PokemonSpeciesInfo species) {
        if (species.isAlternativeForm()) {
            return PokemonNameUtils.matchesBaseSpecies(card, species);
        }
        return PokemonNameUtils.matchesSpecies(card, species);
    }

    private CardBrief cardToBrief(UserCard card) {
        CardBrief brief = new CardBrief();
        brief.setId(card.getCardId());
        brief.setEnglishName(card.getName());
        brief.setFrenchName(card.getFrenchName());
        brief.setFormLabel(card.getFormLabel());
        brief.setImage(card.getImage());
        brief.setSetId(card.getSetId());
        brief.setSetName(card.getSetName());
        return brief;
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String normalized = comment.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
