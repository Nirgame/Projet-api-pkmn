# 🚀 Démarrage Rapide

## Installation en 3 étapes

### 1. Prérequis

- [Java 21+](https://www.oracle.com/java/technologies/downloads/)
- [Maven 3.6+](https://maven.apache.org/download.cgi)

Vérifiez l'installation:

```bash
java -version
mvn -version
```

### 2. Compiler le projet

```bash
mvn clean compile
```

### 3. Exécuter l'exemple

**Toutes les plateformes:**

```bash
mvn exec:java
```

## Utilisation en 2 minutes

### Créer un client

```java
import net.tcgdex.TCGdexClient;

TCGdexClient client = new TCGdexClient("en");  // Client anglais
```

### Récupérer des cartes

```java
// Obtenir la première carte
Card card = client.getCardService().getCard("base4-1");
System.out.println(card.getName());  // Alakazam

// Rechercher
List<CardBrief> results = client.getCardService().searchByName("Pikachu");
results.forEach(c -> System.out.println(c.getName()));
```

### Récupérer les ensembles

```java
// Lister tous les ensembles
List<Set> sets = client.getSetService().listSets();
sets.forEach(s -> System.out.println(s.getName()));

// Obtenir un set spécifique
Set baseSet = client.getSetService().getSet("base4");
```

### Changer de langue

```java
// Créer avec une autre langue
TCGdexClient clientFR = new TCGdexClient("fr");

// Ou changer après création
client.setLanguage("ja");  // Japonais
```

## Exemples d'utilisation courante

### Dans une classe Java

```java
public class PokemonSearcher {
    private TCGdexClient client;

    public PokemonSearcher() {
        this.client = new TCGdexClient("en");
    }

    public void findCard(String name) throws IOException {
        List<CardBrief> results = client.getCardService()
            .searchByName(name);

        results.forEach(card ->
            System.out.println(card.getName() + " - " + card.getId())
        );
    }
}
```

### Ajouter au projet Maven

Ajoutez le JAR compilé à votre classpath:

```bash
mvn clean package
# Le JAR est dans target/pokemon-tcg-client-1.0.0.jar
```

## Dépannage

### Erreur: "Cannot find symbol"

Assurez-vous d'avoir compilé:

```bash
mvn clean compile
```

### Erreur: "Connection refused"

Vérifiez votre connexion Internet. L'API est sur `https://tcgdex.dev`

### Erreur: "404 Not Found"

L'ID de carte n'existe peut-être pas. Essayez:

```bash
List<CardBrief> cards = client.getCardService().listCards();
System.out.println(cards.get(0).getId());  // Un ID valide
```

## Prochaines étapes

1. Lire la [documentation complète](README.md)
2. Explorer les [exemples avancés](ADVANCED_USAGE.md)
3. Consulter la [structure du projet](PROJECT_STRUCTURE.md)
4. Vérifier les [langues supportées](README.md#langues-supportées)

## Ressources

- 📖 [API Documentation](https://tcgdex.dev)
- 🐛 [Signaler un bug](https://github.com/tcgdex/java-sdk/issues)
- 💬 [Discord Community](https://tcgdex.dev/discord)

---

**Vous êtes prêt! Commencez à explorer l'API Pokémon TCG! 🎴**
