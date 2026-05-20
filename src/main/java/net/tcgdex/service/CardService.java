package net.tcgdex.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.Card;
import net.tcgdex.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Service pour accéder aux cartes via l'API TCGdex
 */
public class CardService {
    private static final Logger logger = LoggerFactory.getLogger(CardService.class);
    private final Gson gson = new Gson();
    private String language = "en";

    public CardService() {
    }

    public CardService(String language) {
        this.language = language;
    }

    /**
     * Récupère une liste de cartes avec filtres optionnels
     *
     * @return Liste des cartes
     * @throws IOException en cas d'erreur API
     */
    public List<CardBrief> listCards() throws IOException {
        return listCards("");
    }

    /**
     * Récupère une liste de cartes avec paramètres
     *
     * @param queryParams Paramètres de requête (ex: "q.name:furret")
     * @return Liste des cartes
     * @throws IOException en cas d'erreur API
     */
    public List<CardBrief> listCards(String queryParams) throws IOException {
        logger.info("Récupération des cartes avec paramètres: {}", queryParams);

        String response;
        if (queryParams != null && !queryParams.isEmpty()) {
            response = HttpClientUtil.get("/" + language + "/cards", encodeQuery(queryParams));
        } else {
            response = HttpClientUtil.get("/" + language + "/cards");
        }

        List<CardBrief> cards = gson.fromJson(response,
                new TypeToken<List<CardBrief>>() {
                }.getType());

        logger.info("Cartes récupérées: {}", cards.size());
        return cards;
    }

    private String encodeQuery(String queryParams) {
        String[] parts = queryParams.split("&");
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int idx = part.indexOf('=');
            if (idx > 0) {
                String key = part.substring(0, idx);
                String value = part.substring(idx + 1);
                part = key + "=" + java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
            }
            encoded.append(part);
            if (i < parts.length - 1) {
                encoded.append("&");
            }
        }
        return encoded.toString();
    }

    /**
     * Récupère une carte spécifique par son ID
     *
     * @param cardId L'ID de la carte (ex: "base4-1")
     * @return La carte détaillée
     * @throws IOException en cas d'erreur API
     */
    public Card getCard(String cardId) throws IOException {
        logger.info("Récupération de la carte: {}", cardId);

        String response = HttpClientUtil.get("/" + language + "/cards/" + cardId);
        Card card = gson.fromJson(response, Card.class);

        logger.info("Carte trouvée: {}", card.getName());
        return card;
    }

    /**
     * Récupère une carte par son ID local et l'ID du set
     *
     * @param setId   L'ID du set
     * @param localId L'ID local de la carte dans le set
     * @return La carte
     * @throws IOException en cas d'erreur API
     */
    public Card getCardBySetAndLocal(String setId, String localId) throws IOException {
        logger.info("Récupération de la carte: set={}, localId={}", setId, localId);

        String response = HttpClientUtil.get(
                "/" + language + "/cards/" + setId + "/" + localId);
        Card card = gson.fromJson(response, Card.class);

        return card;
    }

    /**
     * Recherche des cartes par nom
     *
     * @param name Le nom de la carte à rechercher
     * @return Liste des cartes correspondantes
     * @throws IOException en cas d'erreur API
     */
    public List<CardBrief> searchByName(String name) throws IOException {
        String query = "name=" + name;
        return listCards(query);
    }

    /**
     * Modifie la langue pour les requêtes
     *
     * @param language Code de langue (en, fr, ja, etc.)
     */
    public void setLanguage(String language) {
        this.language = language;
        logger.info("Langue définie à: {}", language);
    }

    public String getLanguage() {
        return language;
    }
}
