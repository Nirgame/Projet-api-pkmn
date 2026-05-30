package net.tcgdex.model;

import net.tcgdex.entity.UserCard;

public record PokedexListItem(
        PokemonSpeciesInfo species,
        UserCard assignedCard,
        long ownedCardCount,
        boolean missingCardMarked,
        String comment) {

    public boolean hasAssignedCard() {
        return assignedCard != null;
    }

    public boolean hasMissingCardMarked() {
        return missingCardMarked;
    }

    public boolean hasComment() {
        return comment != null && !comment.isBlank();
    }
}
