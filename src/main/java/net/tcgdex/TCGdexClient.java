package net.tcgdex;

import net.tcgdex.service.CardService;
import net.tcgdex.service.SetService;
import net.tcgdex.service.SerieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client principal pour l'API TCGdex
 * Fournit un accès centralisé à tous les services
 */
public class TCGdexClient {
    private static final Logger logger = LoggerFactory.getLogger(TCGdexClient.class);

    private final CardService cardService;
    private final SetService setService;
    private final SerieService serieService;

    /**
     * Crée un client TCGdex en anglais par défaut
     */
    public TCGdexClient() {
        this("en");
    }

    /**
     * Crée un client TCGdex avec une langue spécifique
     *
     * @param language Code de langue (en, fr, ja, etc.)
     */
    public TCGdexClient(String language) {
        logger.info("Initialisation du client TCGdex avec langue: {}", language);

        this.cardService = new CardService(language);
        this.setService = new SetService(language);
        this.serieService = new SerieService(language);
    }

    /**
     * Obtient le service des cartes
     *
     * @return Service des cartes
     */
    public CardService getCardService() {
        return cardService;
    }

    /**
     * Obtient le service des ensembles
     *
     * @return Service des ensembles
     */
    public SetService getSetService() {
        return setService;
    }

    /**
     * Obtient le service des séries
     *
     * @return Service des séries
     */
    public SerieService getSerieService() {
        return serieService;
    }

    /**
     * Change la langue pour tous les services
     *
     * @param language Code de langue
     */
    public void setLanguage(String language) {
        logger.info("Changement de langue à: {}", language);
        cardService.setLanguage(language);
        setService.setLanguage(language);
        serieService.setLanguage(language);
    }
}
