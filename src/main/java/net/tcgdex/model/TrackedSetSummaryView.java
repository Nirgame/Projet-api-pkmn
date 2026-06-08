package net.tcgdex.model;

public record TrackedSetSummaryView(
        Set set,
        int totalCards,
        long ownedDistinctCards,
        long ownedCopies) {

    public int progressPercent() {
        if (totalCards <= 0) {
            return 0;
        }
        return (int) Math.round((ownedDistinctCards * 100.0) / totalCards);
    }

    public long missingCards() {
        return Math.max(0, totalCards - ownedDistinctCards);
    }

    public boolean isComplete() {
        return totalCards > 0 && ownedDistinctCards >= totalCards;
    }
}
