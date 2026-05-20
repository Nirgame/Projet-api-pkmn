# Guide d'Utilisation Avancé

## Table des matières

1. [Exemples de base](#exemples-de-base)
2. [Filtrage et recherche](#filtrage-et-recherche)
3. [Gestion des erreurs](#gestion-des-erreurs)
4. [Langues multilingues](#langues-multilingues)
5. [Performance](#performance)
6. [Intégration dans une application](#intégration-dans-une-application)

## Exemples de base

### Créer un client

```java
import net.tcgdex.TCGdexClient;

// Client avec langue par défaut (English)
TCGdexClient client = new TCGdexClient();

// Client avec langue spécifique
TCGdexClient client = new TCGdexClient("fr");  // Français
```

### Récupérer les cartes

```java
import net.tcgdex.model.CardBrief;
import java.util.List;

// Lister toutes les cartes
List<CardBrief> allCards = client.getCardService().listCards();

// Obtenir une carte spécifique
Card card = client.getCardService().getCard("base4-1");
System.out.println("Nom: " + card.getName());
System.out.println("Illustrateur: " + card.getIllustrator());
```

### Récupérer les ensembles

```java
import net.tcgdex.model.Set;

// Lister tous les ensembles
List<Set> sets = client.getSetService().listSets();

// Obtenir un ensemble spécifique
Set set = client.getSetService().getSet("base4");
System.out.println("Ensemble: " + set.getName());
System.out.println("Total de cartes: " + set.getTotal());
```

### Récupérer les séries

```java
import net.tcgdex.model.Serie;

// Lister toutes les séries
List<Serie> series = client.getSerieService().listSeries();

// Obtenir une série spécifique
Serie serie = client.getSerieService().getSerie("base");
System.out.println("Série: " + serie.getName());
```

## Filtrage et recherche

### Rechercher par nom

```java
// Rechercher des cartes avec "Furret" dans le nom
List<CardBrief> furrets = client.getCardService().searchByName("Furret");
furrets.forEach(card ->
    System.out.println(card.getName() + " (" + card.getId() + ")")
);
```

### Utiliser les paramètres de requête

```java
// Filtrage personnalisé
String query = "q.name:Pikachu&p.limit=10";
List<CardBrief> cards = client.getCardService().listCards(query);
```

### Récupérer une carte par Set et ID local

```java
// Obtenir une carte spécifique dans un ensemble
Card card = client.getCardService().getCardBySetAndLocal("base4", "1");
System.out.println(card.getName());
```

## Gestion des erreurs

### Gérer les exceptions

```java
try {
    Card card = client.getCardService().getCard("invalid-id");
} catch (IOException e) {
    System.err.println("Erreur d'accès à l'API: " + e.getMessage());
    // Traiter l'erreur
}
```

### Vérifier les réponses

```java
try {
    List<CardBrief> cards = client.getCardService().listCards();

    if (cards == null || cards.isEmpty()) {
        System.out.println("Aucune carte trouvée");
    } else {
        System.out.println("Nombre de cartes: " + cards.size());
    }
} catch (IOException e) {
    System.err.println("Erreur: " + e.getMessage());
}
```

## Langues multilingues

### Langues supportées

| Code | Langue           | Emoji |
| ---- | ---------------- | ----- |
| en   | English          | 🇬🇧    |
| fr   | Français         | 🇫🇷    |
| es   | Español          | 🇪🇸    |
| it   | Italiano         | 🇮🇹    |
| pt   | Português        | 🇵🇹    |
| de   | Deutsch          | 🇩🇪    |
| ja   | 日本語           | 🇯🇵    |
| zh   | 中文             | 🇨🇳    |
| id   | Bahasa Indonesia | 🇮🇩    |
| th   | ไทย              | 🇹🇭    |

### Changer de langue

```java
// Au moment de la création
TCGdexClient client = new TCGdexClient("ja");  // Japonais

// Ou après la création
client.setLanguage("fr");  // Français
client.getCardService().listCards();
```

### Utiliser plusieurs services avec des langues différentes

```java
// Approche 1: Créer plusieurs clients
TCGdexClient clientEN = new TCGdexClient("en");
TCGdexClient clientFR = new TCGdexClient("fr");

List<CardBrief> cardsEN = clientEN.getCardService().listCards();
List<CardBrief> cardsFR = clientFR.getCardService().listCards();

// Approche 2: Changer de langue
TCGdexClient client = new TCGdexClient("en");
List<CardBrief> cardsEN = client.getCardService().listCards();

client.setLanguage("fr");
List<CardBrief> cardsFR = client.getCardService().listCards();
```

## Performance

### Recommandations

1. **Réutiliser le client**: Créez un seul client et réutilisez-le
2. **Cache**: Implémentez un cache pour les requêtes fréquentes
3. **Pagination**: Utilisez les paramètres de pagination pour les grandes listes

### Exemple avec cache simple

```java
import java.util.*;

public class CachedCardService {
    private TCGdexClient client;
    private Map<String, Card> cardCache = new HashMap<>();

    public CachedCardService(TCGdexClient client) {
        this.client = client;
    }

    public Card getCard(String cardId) throws IOException {
        if (cardCache.containsKey(cardId)) {
            return cardCache.get(cardId);
        }

        Card card = client.getCardService().getCard(cardId);
        cardCache.put(cardId, card);
        return card;
    }
}
```

## Intégration dans une application

### Utilisation dans une application serveur

```java
import org.springframework.stereotype.Service;

@Service
public class PokemonCardService {
    private final TCGdexClient tcgdexClient;

    public PokemonCardService() {
        this.tcgdexClient = new TCGdexClient("en");
    }

    public CardBrief getCardDetails(String cardId) throws IOException {
        return tcgdexClient.getCardService().getCard(cardId);
    }

    public List<CardBrief> searchCards(String query) throws IOException {
        return tcgdexClient.getCardService().searchByName(query);
    }
}
```

### Utilisation dans une interface Web (JavaFX)

```java
import javafx.scene.control.ListView;

public class PokemonCardUI {
    private TCGdexClient client = new TCGdexClient("en");
    private ListView<CardBrief> cardList;

    public void searchAndDisplay(String query) throws IOException {
        List<CardBrief> results = client.getCardService().searchByName(query);
        cardList.getItems().addAll(results);
    }
}
```

### Utilisation dans une API REST

```java
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/api/cards")
public class CardResource {
    private TCGdexClient client = new TCGdexClient("en");

    @GET
    @Path("/{cardId}")
    public Card getCard(@PathParam("cardId") String cardId) throws IOException {
        return client.getCardService().getCard(cardId);
    }
}
```

## Ressources additionnelles

- [Documentation TCGdex](https://tcgdex.dev)
- [GitHub Repository](https://github.com/tcgdex/java-sdk)
- [API Reference](https://tcgdex.dev/rest)
