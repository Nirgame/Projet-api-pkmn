package net.tcgdex.util;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utilitaire pour effectuer des requêtes HTTP vers l'API TCGdex
 */
public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final HttpClient httpClient = HttpClients.createDefault();

    /** URL de base de l'API TCGdex */
    private static final String BASE_URL = "https://api.tcgdex.net/v2";

    /**
     * Effectue une requête GET à l'API
     *
     * @param endpoint L'endpoint API (ex: "/cards", "/sets")
     * @return La réponse JSON en tant que String
     * @throws IOException en cas d'erreur réseau
     */
    public static String get(String endpoint) throws IOException {
        return get(endpoint, "");
    }

    /**
     * Effectue une requête GET avec des paramètres de requête
     *
     * @param endpoint    L'endpoint API
     * @param queryParams Les paramètres de requête
     * @return La réponse JSON en tant que String
     * @throws IOException en cas d'erreur réseau
     */
    public static String get(String endpoint, String queryParams) throws IOException {
        String url = BASE_URL + endpoint;
        if (queryParams != null && !queryParams.isEmpty()) {
            url += "?" + queryParams;
        }

        logger.debug("Requête GET complète: {}", url);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        try {
            return httpClient.execute(httpGet, response -> {
                int statusCode = response.getCode();
                logger.debug("Code de réponse: {}", statusCode);

                if (statusCode != 200) {
                    throw new IOException("Erreur HTTP " + statusCode);
                }

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return "";
                }

                StringBuilder result = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(entity.getContent()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }

                return result.toString();
            });
        } catch (IOException e) {
            logger.error("Erreur lors de la requête: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Construit une URL avec langue
     *
     * @param language Code de langue (en, fr, ja, etc.)
     * @return URL de base avec langue
     */
    public static String withLanguage(String language) {
        return BASE_URL.replace("v1", "v1:" + language);
    }
}
