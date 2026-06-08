package net.tcgdex.model;

public record CardPriceView(
        String cardId,
        String cardmarketPrice,
        String tcgplayerPrice) {

    public boolean hasAnyPrice() {
        return (cardmarketPrice != null && !cardmarketPrice.isBlank())
                || (tcgplayerPrice != null && !tcgplayerPrice.isBlank());
    }
}
