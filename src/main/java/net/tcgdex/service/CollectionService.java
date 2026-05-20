package net.tcgdex.service;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.model.Card;
import net.tcgdex.repository.UserCardRepository;
import net.tcgdex.repository.UserPokemonCardAssignmentRepository;
import net.tcgdex.util.CardNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class CollectionService {

    @Autowired
    private UserCardRepository userCardRepository;

    @Autowired
    private TCGdexService tcgdexService;

    @Autowired
    private UserPokemonCardAssignmentRepository assignmentRepository;

    public List<UserCard> getUserCollection(User user) {
        List<UserCard> cards = userCardRepository.findByUser(user);
        cards.forEach(this::refreshStoredMetadataIfNeeded);
        return cards;
    }

    public Optional<UserCard> getUserCard(User user, String cardId) {
        return userCardRepository.findByUserAndCardId(user, cardId);
    }

    public UserCard addCardToCollection(User user, String cardId) {
        Optional<UserCard> existingCard = userCardRepository.findByUserAndCardId(user, cardId);
        if (existingCard.isPresent()) {
            UserCard card = existingCard.get();
            card.setQuantity(card.getQuantity() + 1);
            refreshStoredMetadataIfNeeded(card);
            return userCardRepository.save(card);
        }

        try {
            Card cardDetails = tcgdexService.getCard(cardId);
            UserCard userCard = new UserCard(user, cardId, cardDetails.getName());
            userCard.setFrenchName(cardDetails.getFrenchName());
            userCard.setFormLabel(cardDetails.getFormLabel());
            userCard.setImage(cardDetails.getImage());
            userCard.setSetId(null);
            userCard.setSetName(null);
            return userCardRepository.save(userCard);
        } catch (Exception e) {
            UserCard userCard = new UserCard(user, cardId, "Unknown Card");
            return userCardRepository.save(userCard);
        }
    }

    public boolean removeCardFromCollection(User user, String cardId) {
        Optional<UserCard> userCard = userCardRepository.findByUserAndCardId(user, cardId);
        if (userCard.isPresent()) {
            UserCard card = userCard.get();
            if (card.getQuantity() > 1) {
                card.setQuantity(card.getQuantity() - 1);
                userCardRepository.save(card);
            } else {
                assignmentRepository.deleteByUserAndAssignedCardId(user, cardId);
                userCardRepository.delete(card);
            }
            return true;
        }
        return false;
    }

    public List<UserCard> searchUserCards(User user, String name, String formLabel) {
        List<UserCard> collection = getUserCollection(user);

        List<UserCard> exactMatches = collection.stream()
                .filter(card -> CardNameUtils.matchesExactCard(card, name, formLabel))
                .toList();
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }

        return collection.stream()
                .filter(card -> CardNameUtils.matchesSearch(card, name))
                .toList();
    }

    public List<UserCard> searchUserCards(User user, String name) {
        return searchUserCards(user, name, null);
    }

    public List<String> getDistinctPokemonNames(User user) {
        return getDistinctPokemonCards(user).stream()
                .map(UserCard::getName)
                .toList();
    }

    public List<UserCard> getDistinctPokemonCards(User user) {
        Map<String, UserCard> cardsByPokemon = new LinkedHashMap<>();

        getUserCollection(user).stream()
                .sorted(Comparator.comparing(UserCard::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(card -> {
                    String key = CardNameUtils.normalizeForSearch(card.getName())
                            + "|"
                            + CardNameUtils.normalizeForSearch(card.getFormLabel());
                    if (key.isBlank()) {
                        key = CardNameUtils.normalizeForSearch(card.getFrenchName());
                    }
                    cardsByPokemon.putIfAbsent(key, card);
                });

        return cardsByPokemon.values().stream().toList();
    }

    public long getCollectionSize(User user) {
        return userCardRepository.countByUser(user);
    }

    public void clearCollection(User user) {
        List<UserCard> userCards = userCardRepository.findByUser(user);
        userCardRepository.deleteAll(userCards);
    }

    private void refreshStoredMetadataIfNeeded(UserCard card) {
        boolean needsFrenchName = card.getFrenchName() == null || card.getFrenchName().isBlank();
        boolean needsFormLabel = card.getFormLabel() == null || card.getFormLabel().isBlank();
        String namedFormLabel = needsFormLabel
                ? CardNameUtils.extractNamedFormLabel(card.getName(), card.getFrenchName())
                : null;

        if (namedFormLabel != null) {
            card.setFormLabel(namedFormLabel);
            return;
        }

        if (!needsFrenchName) {
            return;
        }

        try {
            Card details = tcgdexService.getCard(card.getCardId());
            card.setName(details.getName());
            card.setFrenchName(details.getFrenchName());
            card.setFormLabel(details.getFormLabel());
            if (card.getImage() == null || card.getImage().isBlank()) {
                card.setImage(details.getImage());
            }
        } catch (Exception ignored) {
        }
    }
}
