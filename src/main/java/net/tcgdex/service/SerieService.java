package net.tcgdex.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.tcgdex.model.Serie;
import net.tcgdex.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Service pour accéder aux séries de cartes via l'API TCGdex
 */
public class SerieService {
    private static final Logger logger = LoggerFactory.getLogger(SerieService.class);
    private final Gson gson = new Gson();
    private String language = "en";

    public SerieService() {
    }

    public SerieService(String language) {
        this.language = language;
    }

    /**
     * Récupère une liste de toutes les séries
     *
     * @return Liste de toutes les séries
     * @throws IOException en cas d'erreur API
     */
    public List<Serie> listSeries() throws IOException {
        return listSeries("");
    }

    /**
     * Récupère une liste de séries avec filtres
     *
     * @param queryParams Paramètres de requête
     * @return Liste des séries
     * @throws IOException en cas d'erreur API
     */
    public List<Serie> listSeries(String queryParams) throws IOException {
        logger.info("Récupération des séries");

        String response = HttpClientUtil.get("/" + language + "/series", queryParams);
        List<Serie> series = gson.fromJson(response,
                new TypeToken<List<Serie>>() {
                }.getType());

        logger.info("Séries récupérées: {}", series.size());
        return series;
    }

    /**
     * Récupère une série spécifique par son ID
     *
     * @param serieId L'ID de la série
     * @return La série détaillée
     * @throws IOException en cas d'erreur API
     */
    public Serie getSerie(String serieId) throws IOException {
        logger.info("Récupération de la série: {}", serieId);

        String response = HttpClientUtil.get("/" + language + "/series/" + serieId);
        Serie serie = gson.fromJson(response, Serie.class);

        logger.info("Série trouvée: {}", serie.getName());
        return serie;
    }

    /**
     * Modifie la langue pour les requêtes
     *
     * @param language Code de langue
     */
    public void setLanguage(String language) {
        this.language = language;
        logger.info("Langue définie à: {}", language);
    }

    public String getLanguage() {
        return language;
    }
}
