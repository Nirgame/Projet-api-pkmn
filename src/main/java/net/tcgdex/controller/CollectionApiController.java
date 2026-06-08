package net.tcgdex.controller;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.service.CollectionService;
import net.tcgdex.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collection")
public class CollectionApiController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private UserService userService;

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String username = authentication.getName();
        return userService.findByUsername(username).orElseThrow();
    }

    @PostMapping("/cards/{cardId}")
    public ResponseEntity<?> addCard(@PathVariable String cardId, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            UserCard userCard = collectionService.addCardToCollection(user, cardId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "card", Map.of(
                            "id", userCard.getCardId(),
                            "name", userCard.getName(),
                            "quantity", userCard.getQuantity())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<?> removeCard(@PathVariable String cardId, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            boolean removed = collectionService.removeCardFromCollection(user, cardId);
            int quantity = collectionService.getUserCard(user, cardId)
                    .map(UserCard::getQuantity)
                    .orElse(0);
            return ResponseEntity.ok(Map.of(
                    "success", removed,
                    "cardId", cardId,
                    "quantity", quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/cards")
    public ResponseEntity<List<UserCard>> getCollection(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<UserCard> collection = collectionService.getUserCollection(user);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        User user = getCurrentUser(authentication);
        long collectionSize = collectionService.getCollectionSize(user);
        List<String> pokemonNames = collectionService.getDistinctPokemonNames(user);

        return ResponseEntity.ok(Map.of(
                "totalCards", collectionSize,
                "uniquePokemon", pokemonNames.size(),
                "pokemonNames", pokemonNames));
    }
}
