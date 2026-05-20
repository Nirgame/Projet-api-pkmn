package net.tcgdex.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.tcgdex.model.Set;
import net.tcgdex.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Service pour accéder aux ensembles de cartes via l'API TCGdex
 */
public class SetService {
    private static final Logger logger = LoggerFactory.getLogger(SetService.class);
    private final Gson gson = new Gson();
    private String language = "en";

    public SetService() {
    }

    public SetService(String language) {
        this.language = language;
    }

    /**
     * Récupère une liste de tous les ensembles
     *
     * @return Liste de tous les ensembles
     * @throws IOException en cas d'erreur API
     */
    public List<Set> listSets() throws IOException {
        return listSets("");
    }

    /**
     * Récupère une liste d'ensembles avec filtres
     *
     * @param queryParams Paramètres de requête
     * @return Liste des ensembles
     * @throws IOException en cas d'erreur API
     */
    public List<Set> listSets(String queryParams) throws IOException {
        logger.info("Récupération des ensembles");

        String response = HttpClientUtil.get("/" + language + "/sets", queryParams);
        List<Set> sets = gson.fromJson(response,
                new TypeToken<List<Set>>() {
                }.getType());

        logger.info("Ensembles récupérés: {}", sets.size());
        return sets;
    }

    /**
     * Récupère un ensemble spécifique par son ID
     *
     * @param setId L'ID de l'ensemble
     * @return L'ensemble détaillé
     * @throws IOException en cas d'erreur API
     */
    public Set getSet(String setId) throws IOException {
        logger.info("Récupération de l'ensemble: {}", setId);

        String response = HttpClientUtil.get("/" + language + "/sets/" + setId);
        Set set = gson.fromJson(response, Set.class);

        logger.info("Ensemble trouvé: {}", set.getName());
        return set;
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
