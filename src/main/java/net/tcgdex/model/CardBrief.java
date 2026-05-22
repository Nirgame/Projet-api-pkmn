package net.tcgdex.model;

import net.tcgdex.util.CardNameUtils;

/**
 * ReprÃ©sente une brÃ¨ve information sur une carte PokÃ©mon
 */
public class CardBrief {
    private String id;
    private String localId;
    private String name;
    private String englishName;
    private String frenchName;
    private String englishRarity;
    private String frenchRarity;
    private String formLabel;
    private String image;
    private String setId;
    private String setName;

    public CardBrief() {
    }

    public CardBrief(String id, String localId, String name, String image) {
        this.id = id;
        this.localId = localId;
        this.name = name;
        this.englishName = name;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getName() {
        return getEnglishName();
    }

    public void setName(String name) {
        this.name = name;
        if (this.englishName == null || this.englishName.isBlank()) {
            this.englishName = name;
        }
    }

    public String getEnglishName() {
        return englishName != null ? englishName : name;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
        if (this.name == null || this.name.isBlank()) {
            this.name = englishName;
        }
    }

    public String getFrenchName() {
        return frenchName;
    }

    public void setFrenchName(String frenchName) {
        this.frenchName = frenchName;
    }

    public String getFormLabel() {
        return formLabel;
    }

    public void setFormLabel(String formLabel) {
        this.formLabel = formLabel;
    }

    public boolean hasFrenchName() {
        return frenchName != null && !frenchName.isBlank();
    }

    public boolean hasDifferentFrenchName() {
        return hasFrenchName() && !getEnglishName().equalsIgnoreCase(frenchName);
    }

    public String getDisplayName() {
        if (hasFrenchName()) {
            return frenchName;
        }
        return getEnglishName();
    }

    public String getSecondaryName() {
        if (hasDifferentFrenchName()) {
            return getEnglishName();
        }
        return null;
    }

    public String getVariantLabel() {
        if (formLabel != null && !formLabel.isBlank()) {
            return formLabel;
        }
        return CardNameUtils.extractVariantLabel(getEnglishName(), frenchName);
    }

    public String getEnglishRarity() {
        return englishRarity;
    }

    public void setEnglishRarity(String englishRarity) {
        this.englishRarity = englishRarity;
    }

    public String getFrenchRarity() {
        return frenchRarity;
    }

    public void setFrenchRarity(String frenchRarity) {
        this.frenchRarity = frenchRarity;
    }

    public String getDisplayRarity() {
        if (frenchRarity != null && !frenchRarity.isBlank()) {
            return frenchRarity;
        }
        return englishRarity;
    }

    public boolean hasRarity() {
        return getDisplayRarity() != null && !getDisplayRarity().isBlank();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDisplayImage() {
        if (image == null || image.isBlank()) {
            return "/images/placeholder.svg";
        }
        if (image.endsWith(".png") || image.endsWith(".jpg") || image.endsWith(".jpeg") || image.endsWith(".webp")) {
            return image;
        }
        return image + "/high.webp";
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getResolvedSetId() {
        if (setId != null && !setId.isBlank()) {
            return setId;
        }
        if (id == null || id.isBlank() || !id.contains("-")) {
            return null;
        }
        return id.substring(0, id.indexOf('-'));
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public String getDisplaySetName() {
        if (setName != null && !setName.isBlank()) {
            return setName;
        }
        return getResolvedSetId();
    }

    @Override
    public String toString() {
        return "CardBrief{" +
                "id='" + id + '\'' +
                ", localId='" + localId + '\'' +
                ", englishName='" + getEnglishName() + '\'' +
                ", frenchName='" + frenchName + '\'' +
                ", englishRarity='" + englishRarity + '\'' +
                ", frenchRarity='" + frenchRarity + '\'' +
                ", formLabel='" + formLabel + '\'' +
                ", image='" + image + '\'' +
                ", setId='" + setId + '\'' +
                ", setName='" + setName + '\'' +
                '}';
    }
}
