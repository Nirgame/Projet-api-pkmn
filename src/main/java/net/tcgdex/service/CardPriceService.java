package net.tcgdex.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.tcgdex.model.CardPriceView;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CardPriceService {

    private static final String API_BASE_URL = "https://api.pokemontcg.io/v2/cards/";
    private static final List<String> TCGPLAYER_PRICE_KEYS = List.of(
            "holofoil",
            "normal",
            "reverseHolofoil",
            "1stEditionHolofoil",
            "1stEditionNormal",
            "unlimitedHolofoil",
            "unlimitedNormal");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private final Map<String, CardPriceView> priceCache = new ConcurrentHashMap<>();

    public CardPriceView getPrices(String cardId) throws IOException {
        if (cardId == null || cardId.isBlank()) {
            return new CardPriceView("", null, null);
        }

        CardPriceView cachedPrice = priceCache.get(cardId);
        if (cachedPrice != null) {
            return cachedPrice;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + cardId))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Erreur HTTP " + response.statusCode());
            }

            CardPriceView parsedPrice = parsePriceResponse(cardId, response.body());
            priceCache.put(cardId, parsedPrice);
            return parsedPrice;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Requete de prix interrompue", exception);
        }
    }

    private CardPriceView parsePriceResponse(String cardId, String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                ? root.getAsJsonObject("data")
                : new JsonObject();

        String cardmarketPrice = null;
        if (data.has("cardmarket") && data.get("cardmarket").isJsonObject()) {
            JsonObject cardmarket = data.getAsJsonObject("cardmarket");
            JsonObject prices = cardmarket.has("prices") && cardmarket.get("prices").isJsonObject()
                    ? cardmarket.getAsJsonObject("prices")
                    : null;
            if (prices != null) {
                cardmarketPrice = firstAvailablePrice(prices, List.of(
                        "trendPrice",
                        "averageSellPrice",
                        "lowPriceExPlus",
                        "lowPrice"));
                if (cardmarketPrice != null) {
                    cardmarketPrice += " EUR";
                }
            }
        }

        String tcgplayerPrice = null;
        if (data.has("tcgplayer") && data.get("tcgplayer").isJsonObject()) {
            JsonObject tcgplayer = data.getAsJsonObject("tcgplayer");
            JsonObject prices = tcgplayer.has("prices") && tcgplayer.get("prices").isJsonObject()
                    ? tcgplayer.getAsJsonObject("prices")
                    : null;
            if (prices != null) {
                Map<String, JsonObject> typedPrices = new LinkedHashMap<>();
                for (String key : TCGPLAYER_PRICE_KEYS) {
                    if (prices.has(key) && prices.get(key).isJsonObject()) {
                        typedPrices.put(key, prices.getAsJsonObject(key));
                    }
                }
                for (JsonObject value : typedPrices.values()) {
                    tcgplayerPrice = firstAvailablePrice(value, List.of("market", "mid", "low"));
                    if (tcgplayerPrice != null) {
                        tcgplayerPrice += " USD";
                        break;
                    }
                }
            }
        }

        return new CardPriceView(cardId, cardmarketPrice, tcgplayerPrice);
    }

    private String firstAvailablePrice(JsonObject priceObject, List<String> keys) {
        for (String key : keys) {
            if (!priceObject.has(key)) {
                continue;
            }
            JsonElement element = priceObject.get(key);
            if (element == null || element.isJsonNull()) {
                continue;
            }
            try {
                BigDecimal decimal = element.getAsBigDecimal().setScale(2, RoundingMode.HALF_UP);
                return decimal.toPlainString();
            } catch (RuntimeException ignored) {
            }
        }
        return null;
    }
}
