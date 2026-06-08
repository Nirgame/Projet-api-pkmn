package net.tcgdex.model;

import java.util.List;
import java.util.Map;

public record TrackedSetDetailView(
        Set set,
        List<CardBrief> cards,
        Map<String, Integer> ownedCardCounts,
        long ownedDistinctCards,
        long ownedCopies) {

    public int totalCards() {
        return cards == null ? 0 : cards.size();
    }

    public int progressPercent() {
        int totalCards = totalCards();
        if (totalCards <= 0) {
            return 0;
        }
        return (int) Math.round((ownedDistinctCards * 100.0) / totalCards);
    }
}
