package net.tcgdex.controller;

import net.tcgdex.model.Card;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.CardPriceView;
import net.tcgdex.model.Set;
import net.tcgdex.model.Serie;
import net.tcgdex.service.CardPriceService;
import net.tcgdex.service.TCGdexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TCGdexApiController {

    @Autowired
    private TCGdexService tcgdexService;

    @Autowired
    private CardPriceService cardPriceService;

    @GetMapping("/cards")
    public ResponseEntity<?> getCards(@RequestParam(required = false) String q) {
        try {
            List<CardBrief> cards;
            if (q != null && !q.trim().isEmpty()) {
                cards = tcgdexService.searchCards(q);
            } else {
                cards = tcgdexService.getCards();
            }
            return ResponseEntity.ok(cards);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Unable to fetch cards"));
        }
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<?> getCard(@PathVariable String cardId) {
        try {
            Card card = tcgdexService.getCard(cardId);
            return ResponseEntity.ok(card);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cards/{cardId}/prices")
    public ResponseEntity<?> getCardPrices(@PathVariable String cardId) {
        try {
            CardPriceView priceView = cardPriceService.getPrices(cardId);
            return ResponseEntity.ok(priceView);
        } catch (IOException e) {
            return ResponseEntity.ok(new CardPriceView(cardId, null, null));
        }
    }

    @GetMapping("/sets")
    public ResponseEntity<?> getSets() {
        try {
            List<Set> sets = tcgdexService.getSets();
            return ResponseEntity.ok(sets);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Unable to fetch sets"));
        }
    }

    @GetMapping("/sets/{setId}")
    public ResponseEntity<?> getSet(@PathVariable String setId) {
        try {
            Set set = tcgdexService.getSet(setId);
            return ResponseEntity.ok(set);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/series")
    public ResponseEntity<?> getSeries() {
        try {
            List<Serie> series = tcgdexService.getSeries();
            return ResponseEntity.ok(series);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Unable to fetch series"));
        }
    }

    @GetMapping("/series/{serieId}")
    public ResponseEntity<?> getSerie(@PathVariable String serieId) {
        try {
            Serie serie = tcgdexService.getSerie(serieId);
            return ResponseEntity.ok(serie);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
