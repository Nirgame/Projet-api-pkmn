package net.tcgdex.model;

import java.util.List;

/**
 * ReprÃ©sente une carte PokÃ©mon dÃ©taillÃ©e
 */
public class Card extends CardBrief {
    private String category;
    private String illustrator;
    private String rarity;
    private String cardType;
    private Object variants;
    private List<Integer> dexId;
    private List<String> types;
    private String stage;
    private String evolveFrom;
    private List<Ability> abilities;
    private List<Attack> attacks;

    public Card() {
        super();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIllustrator() {
        return illustrator;
    }

    public void setIllustrator(String illustrator) {
        this.illustrator = illustrator;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public Object getVariants() {
        return variants;
    }

    public void setVariants(Object variants) {
        this.variants = variants;
    }

    public List<Integer> getDexId() {
        return dexId;
    }

    public void setDexId(List<Integer> dexId) {
        this.dexId = dexId;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getEvolveFrom() {
        return evolveFrom;
    }

    public void setEvolveFrom(String evolveFrom) {
        this.evolveFrom = evolveFrom;
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
    }

    public List<Attack> getAttacks() {
        return attacks;
    }

    public void setAttacks(List<Attack> attacks) {
        this.attacks = attacks;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", illustrator='" + illustrator + '\'' +
                ", rarity='" + rarity + '\'' +
                ", cardType='" + cardType + '\'' +
                '}';
    }

    public static class Ability {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Attack {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
