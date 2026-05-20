package net.tcgdex.model;

import net.tcgdex.entity.UserCard;

public record PokedexListItem(
        PokemonSpeciesInfo species,
        UserCard assignedCard,
        long ownedCardCount,
        boolean missingCardMarked) {

    public boolean hasAssignedCard() {
        return assignedCard != null;
    }

    public boolean hasMissingCardMarked() {
        return missingCardMarked;
    }
}
