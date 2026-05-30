package net.tcgdex.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.tcgdex.util.CardNameUtils;
import net.tcgdex.util.PokepediaUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_cards")
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "card_id", nullable = false)
    private String cardId;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(name = "french_name")
    private String frenchName;

    @Column(name = "form_label")
    private String formLabel;

    @Column(length = 1000)
    private String image;

    @Column(name = "set_id")
    private String setId;

    @Column(name = "set_name")
    private String setName;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "quantity")
    private Integer quantity = 1;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    // Constructors
    public UserCard() {
    }

    public UserCard(User user, String cardId, String name) {
        this.user = user;
        this.cardId = cardId;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrenchName() {
        return frenchName;
    }

    public void setFrenchName(String frenchName) {
        this.frenchName = frenchName;
    }

    public boolean hasDifferentFrenchName() {
        return frenchName != null && !frenchName.isBlank() && !name.equalsIgnoreCase(frenchName);
    }

    public String getDisplayName() {
        if (frenchName != null && !frenchName.isBlank()) {
            return frenchName;
        }
        return name;
    }

    public String getSecondaryName() {
        if (hasDifferentFrenchName()) {
            return name;
        }
        return null;
    }

    public String getVariantLabel() {
        if (formLabel != null && !formLabel.isBlank()) {
            return formLabel;
        }
        return CardNameUtils.extractVariantLabel(name, frenchName);
    }

    public String getFormLabel() {
        return formLabel;
    }

    public void setFormLabel(String formLabel) {
        this.formLabel = formLabel;
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

    public String getPokepediaUrl() {
        return PokepediaUtils.buildPokemonUrl(getDisplayName(), name);
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
        if (cardId == null || cardId.isBlank() || !cardId.contains("-")) {
            return null;
        }
        return cardId.substring(0, cardId.indexOf('-'));
    }

    public String getResolvedLocalId() {
        if (cardId == null || cardId.isBlank() || !cardId.contains("-")) {
            return null;
        }
        return cardId.substring(cardId.indexOf('-') + 1);
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

    public String getCardmarketUrl() {
        String query = getDisplayName();
        if (query == null || query.isBlank()) {
            query = name;
        }
        if (query == null || query.isBlank()) {
            query = cardId;
        }
        return "https://www.cardmarket.com/fr/Pokemon/Products/Search?category=-1&searchString="
                + URLEncoder.encode(query == null ? "" : query, StandardCharsets.UTF_8)
                + "&searchMode=v2";
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
