package net.tcgdex;

import net.tcgdex.model.CardBrief;
import net.tcgdex.model.Card;
import net.tcgdex.model.Set;
import net.tcgdex.model.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Exemple d'utilisation du client TCGdex
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            // Créer un client TCGdex
            TCGdexClient client = new TCGdexClient("en");
            logger.info("=== TCGdex API Client ===");

            // Exemple 1: Récupérer une liste de séries
            logger.info("\n--- Exemple 1: Récupération des séries ---");
            List<Serie> series = client.getSerieService().listSeries();
            series.stream().limit(3).forEach(s -> logger.info("Série: {} ({})", s.getName(), s.getId()));

            // Exemple 2: Récupérer une liste d'ensembles
            logger.info("\n--- Exemple 2: Récupération des ensembles ---");
            List<Set> sets = client.getSetService().listSets();
            sets.stream().limit(3)
                    .forEach(s -> logger.info("Ensemble: {} ({}, {} cartes)", s.getName(), s.getId(), s.getTotal()));

            // Exemple 3: Récupérer une carte spécifique
            logger.info("\n--- Exemple 3: Récupération d'une carte spécifique ---");
            Card card = client.getCardService().getCard("base4-1");
            logger.info("Carte trouvée: {}", card);

            // Exemple 4: Rechercher des cartes par nom
            logger.info("\n--- Exemple 4: Recherche de cartes ---");
            List<CardBrief> furrets = client.getCardService().searchByName("Furret");
            logger.info("Cartes trouvées ({}):", furrets.size());
            furrets.stream().limit(5).forEach(c -> logger.info("  - {} ({})", c.getName(), c.getId()));

            // Exemple 5: Changement de langue
            logger.info("\n--- Exemple 5: Changement de langue (Français) ---");
            client.setLanguage("fr");
            Serie frenchSerie = client.getSerieService().listSeries().get(0);
            logger.info("Première série en français: {}", frenchSerie.getName());

            logger.info("\n=== Fin de l'exemple ===");

        } catch (IOException e) {
            logger.error("Erreur lors de l'appel API", e);
        }
    }
}
