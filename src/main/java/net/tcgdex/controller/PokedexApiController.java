package net.tcgdex.controller;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.service.PokedexService;
import net.tcgdex.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/pokedex")
public class PokedexApiController {

    private final PokedexService pokedexService;
    private final UserService userService;

    public PokedexApiController(PokedexService pokedexService, UserService userService) {
        this.pokedexService = pokedexService;
        this.userService = userService;
    }

    @PostMapping("/pokemon/{pokemonId}/assign/{cardId}")
    public ResponseEntity<?> assignCard(@PathVariable int pokemonId,
            @PathVariable String cardId,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            UserCard assignedCard = pokedexService.assignCard(user, pokemonId, cardId) != null
                    ? pokedexService.getAssignedCard(user, pokemonId).orElse(null)
                    : null;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "pokemonId", pokemonId,
                    "cardId", cardId,
                    "cardName", assignedCard != null ? assignedCard.getName() : ""));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", exception.getMessage()));
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", exception.getMessage()));
        }
    }

    @PostMapping("/pokemon/{pokemonId}/mark-missing")
    public ResponseEntity<?> markMissingCard(@PathVariable int pokemonId, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            pokedexService.markMissingCard(user, pokemonId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "pokemonId", pokemonId,
                    "missingCard", true));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", exception.getMessage()));
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", exception.getMessage()));
        }
    }

    @DeleteMapping("/pokemon/{pokemonId}/assignment")
    public ResponseEntity<?> clearAssignment(@PathVariable int pokemonId, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            pokedexService.clearAssignment(user, pokemonId);
            return ResponseEntity.ok(Map.of("success", true, "pokemonId", pokemonId));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", exception.getMessage()));
        }
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifie.");
        }

        return userService.findByUsername(authentication.getName()).orElseThrow();
    }
}
