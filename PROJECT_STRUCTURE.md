# Structure du Projet

## Répertoire et fichiers

```
pokemon-tcg-client/
├── pom.xml                           # Configuration Maven
├── README.md                         # Documentation principale
├── ADVANCED_USAGE.md                 # Guide d'utilisation avancée
├── CHANGELOG.md                      # Historique des modifications
├── .gitignore                        # Fichiers à ignorer dans Git
│
├── src/
│   ├── main/java/net/tcgdex/
│   │   │
│   │   ├── TCGdexClient.java         # Client principal
│   │   ├── App.java                  # Exemple d'utilisation
│   │   │
│   │   ├── model/
│   │   │   ├── Card.java             # Modèle Card
│   │   │   ├── CardBrief.java        # Modèle CardBrief (pour les listes)
│   │   │   ├── Set.java              # Modèle Set
│   │   │   └── Serie.java            # Modèle Serie
│   │   │
│   │   ├── service/
│   │   │   ├── CardService.java      # Service des cartes
│   │   │   ├── SetService.java       # Service des ensembles
│   │   │   └── SerieService.java     # Service des séries
│   │   │
│   │   └── util/
│   │       └── HttpClientUtil.java   # Utilitaire HTTP
│   │
│   ├── main/resources/
│   │   └── logback.xml               # Configuration logging
│   │
│   └── test/java/net/tcgdex/service/
│       ├── CardServiceTest.java      # Tests CardService
│       └── SetServiceTest.java       # Tests SetService
│
└── target/                           # Résultats de compilation
    └── classes/                      # Fichiers .class compilés
```

## Architecture

### Couches

```
┌─────────────────────────────────────────┐
│         Application Client              │  <- App.java
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│  TCGdexClient (Orchestrateur)           │
│  - CardService                          │
│  - SetService                           │
│  - SerieService                         │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│         Services Métier                 │
│  - CardService.java                     │
│  - SetService.java                      │
│  - SerieService.java                    │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│          Utilitaire HTTP                │
│  - HttpClientUtil.java                  │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│      API TCGdex (https://tcgdex.dev)    │
└─────────────────────────────────────────┘
```

## Diagramme de classes

```
┌─────────────────────────────────────────┐
│       TCGdexClient                      │
├─────────────────────────────────────────┤
│ - cardService: CardService              │
│ - setService: SetService                │
│ - serieService: SerieService            │
├─────────────────────────────────────────┤
│ + getCardService(): CardService         │
│ + getSetService(): SetService           │
│ + getSerieService(): SerieService       │
│ + setLanguage(String): void             │
└─────────────────────────────────────────┘

┌─────────────────────┐     ┌────────┐
│  CardService        │────▶│ Card   │
├─────────────────────┤     ├────────┤
│ - language: String  │     │ + id   │
│ - gson: Gson        │     │ + name │
├─────────────────────┤     │ + image│
│ + listCards()       │     └────────┘
│ + getCard(id)       │
│ + searchByName()    │     ┌─────────────┐
│ + setLanguage()     │────▶│ CardBrief   │
└─────────────────────┘     ├─────────────┤
                            │ + id        │
                            │ + name      │
┌─────────────────────┐     └─────────────┘
│  SetService         │
├─────────────────────┤     ┌────────┐
│ - language: String  │────▶│ Set    │
│ - gson: Gson        │     ├────────┤
├─────────────────────┤     │ + id   │
│ + listSets()        │     │ + name │
│ + getSet(id)        │     └────────┘
│ + setLanguage()     │
└─────────────────────┘

┌─────────────────────┐     ┌────────┐
│  SerieService       │────▶│ Serie  │
├─────────────────────┤     ├────────┤
│ - language: String  │     │ + id   │
│ - gson: Gson        │     │ + name │
├─────────────────────┤     └────────┘
│ + listSeries()      │
│ + getSerie(id)      │
│ + setLanguage()     │
└─────────────────────┘

┌─────────────────────────────────┐
│    HttpClientUtil               │
├─────────────────────────────────┤
│ - BASE_URL: String              │
│ - httpClient: HttpClient        │
├─────────────────────────────────┤
│ + get(endpoint): String         │
│ + get(endpoint, params): String │
│ + withLanguage(code): String    │
└─────────────────────────────────┘
```

## Flux de données

```
1. Utilisateur crée TCGdexClient
   │
   └─> TCGdexClient.__init__(language)
       ├─> CardService.setLanguage()
       ├─> SetService.setLanguage()
       └─> SerieService.setLanguage()

2. Utilisateur appelle service.listCards()
   │
   └─> CardService.listCards()
       │
       └─> HttpClientUtil.get("/cards:language")
           │
           ├─> HTTP GET Request
           │
           └─> JSON Response
               │
               └─> Gson.fromJson()
                   │
                   └─> List<CardBrief>

3. Résultat retourné à l'utilisateur
```

## Imports principales

```java
// Client
import net.tcgdex.TCGdexClient;

// Services
import net.tcgdex.service.CardService;
import net.tcgdex.service.SetService;
import net.tcgdex.service.SerieService;

// Modèles
import net.tcgdex.model.Card;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.Set;
import net.tcgdex.model.Serie;

// Utilitaires
import net.tcgdex.util.HttpClientUtil;

// Dépendances
import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
```

## Configuration Maven

### Build Commands

```bash
# Compiler
mvn clean compile

# Tester
mvn test

# Empaqueter
mvn clean package

# Installer en local
mvn clean install

# Exécuter l'exemple
mvn exec:java -Dexec.mainClass="net.tcgdex.App"

# Générer documentation
mvn javadoc:javadoc
```

### Versions

- Java: 11+
- Maven: 3.6+
- Dependencies are automatically resolved from Maven Central

## Points d'extension

Pour étendre le projet:

1. **Ajouter un nouveau service**: Créer une classe dans `service/`
2. **Ajouter un nouveau modèle**: Créer une classe dans `model/`
3. **Personnaliser les requêtes HTTP**: Modifier `HttpClientUtil.java`
4. **Ajouter du logging**: Configurer `logback.xml`
