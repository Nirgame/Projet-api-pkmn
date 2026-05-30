package net.tcgdex.model;

import net.tcgdex.entity.UserCard;

import java.util.List;
import java.util.Map;

public record PokedexDetailView(
        PokemonSpeciesInfo species,
        UserCard assignedCard,
        List<UserCard> ownedCards,
        List<CardBrief> availableCards,
        Map<String, Integer> ownedCardCounts,
        boolean missingCardMarked,
        String comment,
        PokemonSpeciesInfo previousSpecies,
        PokemonSpeciesInfo nextSpecies) {

    public boolean hasAssignedCard() {
        return assignedCard != null;
    }

    public boolean hasMissingCardMarked() {
        return missingCardMarked;
    }

    public boolean hasComment() {
        return comment != null && !comment.isBlank();
    }

    public boolean hasPreviousSpecies() {
        return previousSpecies != null;
    }

    public boolean hasNextSpecies() {
        return nextSpecies != null;
    }
}
