# Pokemon TCG Collection Manager

Une application web complète pour gérer votre collection de cartes Pokemon TCG avec authentification utilisateur et interface moderne.

## 🚀 Fonctionnalités

### ✅ Interface Utilisateur

- **🏠 Page d'accueil** : Présentation et statistiques générales
- **🔍 Parcours des cartes** : Navigation paginée avec recherche
- **📚 Ma collection** : Gestion personnelle des cartes possédées
- **🐉 Vue par Pokémon** : Organisation des cartes par espèce
- **📦 Ensembles** : Exploration de tous les sets disponibles

### ✅ Gestion des Collections

- **➕ Ajouter des cartes** : Interface intuitive pour ajouter des cartes
- **➖ Retirer des cartes** : Gestion des quantités
- **🔢 Comptage automatique** : Statistiques en temps réel
- **🏷️ Organisation par Pokémon** : Vue filtrée par espèce

### ✅ Authentification & Sécurité

- **🔐 Connexion/Inscription** : Système sécurisé
- **🔒 Mots de passe cryptés** : BCrypt encoding
- **👤 Sessions utilisateur** : Gestion des sessions
- **🛡️ Protection CSRF** : Sécurité renforcée

### ✅ API REST

- **📡 API publique** : Accès aux données TCGdex
- **🔑 API privée** : Gestion des collections utilisateur
- **📊 Statistiques** : Métriques de collection

## 🛠️ Technologies Utilisées

- **Backend**: Spring Boot 3.2.0
- **Frontend**: Thymeleaf + Bootstrap 5
- **Base de données**: H2 Database
- **Sécurité**: Spring Security
- **API externe**: TCGdex API v2
- **Build**: Maven

## 📋 Prérequis

- Java 21+
- Maven 3.6+

## 🚀 Démarrage Rapide

### 1. Cloner et compiler

```bash
git clone <repository-url>
cd pokemon-tcg-collection
mvn clean compile
```

### 2. Lancer l'application

```bash
mvn spring-boot:run
```

### 3. Accéder à l'application

Ouvrez votre navigateur à l'adresse : http://localhost:8080

### 4. Comptes de test

- **admin** / password
- **trainer1** / password
- **collector** / password

## 📁 Structure du Projet

```
src/main/java/net/tcgdex/
├── PokemonTcgCollectionApplication.java    # Classe principale
├── config/
│   └── SecurityConfig.java                 # Configuration sécurité
├── controller/
│   ├── WebController.java                  # Contrôleurs web
│   ├── CollectionApiController.java        # API collection
│   └── TCGdexApiController.java            # API TCGdex
├── entity/
│   ├── User.java                           # Entité utilisateur
│   └── UserCard.java                       # Entité carte utilisateur
├── repository/
│   ├── UserRepository.java                 # Repository utilisateur
│   └── UserCardRepository.java             # Repository cartes
├── service/
│   ├── UserService.java                    # Service utilisateur
│   ├── CollectionService.java              # Service collection
│   └── TCGdexService.java                  # Service TCGdex
└── [ancien code TCGdex client]

src/main/resources/
├── templates/                              # Templates Thymeleaf
│   ├── layout.html                         # Layout principal
│   ├── home.html                          # Page d'accueil
│   ├── login.html                         # Connexion
│   ├── register.html                      # Inscription
│   ├── dashboard.html                     # Dashboard utilisateur
│   ├── browse.html                        # Parcours des cartes
│   ├── collection.html                    # Collection personnelle
│   ├── pokemon.html                       # Cartes par Pokémon
│   └── sets.html                          # Ensembles
├── static/                                 # Ressources statiques
├── application.properties                 # Configuration
└── data.sql                              # Données de test
```

## 🔗 Endpoints API

### API Publique (TCGdex)

- `GET /api/cards` - Liste des cartes
- `GET /api/cards/{id}` - Détails d'une carte
- `GET /api/sets` - Liste des ensembles
- `GET /api/sets/{id}` - Détails d'un ensemble
- `GET /api/series` - Liste des séries

### API Privée (Collection)

- `POST /api/collection/cards/{cardId}` - Ajouter une carte
- `DELETE /api/collection/cards/{cardId}` - Retirer une carte
- `GET /api/collection/cards` - Ma collection
- `GET /api/collection/stats` - Statistiques

## 🎨 Interface Utilisateur

### Page d'accueil

- Statistiques générales
- Navigation vers les différentes sections
- Présentation des fonctionnalités

### Parcours des cartes

- Recherche en temps réel
- Pagination
- Ajout direct à la collection
- Indicateurs de possession

### Ma Collection

- Vue d'ensemble avec statistiques
- Filtrage par Pokémon
- Gestion des quantités
- Suppression de cartes

### Vue par Pokémon

- Toutes les cartes d'un Pokémon
- Gestion individuelle
- Navigation facile

## 🔒 Sécurité

- **Authentification**: Spring Security avec formulaires personnalisés
- **Autorisation**: Contrôle d'accès basé sur les rôles
- **Cryptage**: Mots de passe hashés avec BCrypt
- **Sessions**: Gestion automatique des sessions utilisateur
- **CSRF**: Protection contre les attaques CSRF

## 🗄️ Base de Données

### Tables

- **users**: Utilisateurs (username, password hashé)
- **user_cards**: Cartes possédées (user_id, card_id, quantity, etc.)

## 🧪 Tests

```bash
mvn test
```

## 📦 Déploiement

### JAR exécutable

```bash
mvn clean package
java -jar target/pokemon-tcg-collection-2.0.0.jar
```

### Variables d'environnement

```bash
export SPRING_PROFILES_ACTIVE=production
export DATABASE_URL=jdbc:postgresql://localhost:5432/pokemon_tcg
export DATABASE_USERNAME=your_username
export DATABASE_PASSWORD=your_password
```

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📝 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🙏 Remerciements

- [TCGdex](https://tcgdex.dev) pour l'API des cartes Pokemon
- [Spring Boot](https://spring.io/projects/spring-boot) pour le framework
- [Bootstrap](https://getbootstrap.com/) pour le CSS framework
- [Thymeleaf](https://www.thymeleaf.org/) pour les templates

---

**🎴 Prêt à collectionner ? Commencez dès maintenant ! 🎴**
TCGdexClient client = new TCGdexClient();

// Ou avec une langue spécifique
TCGdexClient client = new TCGdexClient("fr"); // Français

// Récupérer une liste de cartes
List<CardBrief> cards = client.getCardService().listCards();

// Rechercher une carte spécifique
Card card = client.getCardService().getCard("base4-1");

// Rechercher par nom
List<CardBrief> furrets = client.getCardService().searchByName("Furret");

// Récupérer les ensembles
List<Set> sets = client.getSetService().listSets();

// Récupérer les séries
List<Serie> series = client.getSerieService().listSeries();

// Changer de langue
client.setLanguage("ja"); // Japonais

````

### Accès aux services

```java
TCGdexClient client = new TCGdexClient();

// Service des cartes
CardService cardService = client.getCardService();
cardService.listCards();
cardService.getCard(cardId);
cardService.searchByName(name);

// Service des ensembles
SetService setService = client.getSetService();
setService.listSets();
setService.getSet(setId);

// Service des séries
SerieService serieService = client.getSerieService();
serieService.listSeries();
serieService.getSerie(serieId);
````

## API Endpoints Supportés

### Cartes

- `GET /cards:{language}` - Lister les cartes
- `GET /cards:{language}/{cardId}` - Obtenir une carte
- `GET /cards:{language}/{setId}/{localId}` - Obtenir une carte par set et ID local

### Ensembles

- `GET /sets:{language}` - Lister les ensembles
- `GET /sets:{language}/{setId}` - Obtenir un ensemble

### Séries

- `GET /series:{language}` - Lister les séries
- `GET /series:{language}/{serieId}` - Obtenir une série

## Langues Supportées

- 🇬🇧 `en` - English
- 🇫🇷 `fr` - Français
- 🇪🇸 `es` - Español
- 🇮🇹 `it` - Italiano
- 🇵🇹 `pt` - Português (Brésilien)
- 🇩🇪 `de` - Deutsch
- 🇯🇵 `ja` - 日本語
- 🇨🇳 `zh` - 中文 (Traditionnel)
- 🇮🇩 `id` - Bahasa Indonesia
- 🇹🇭 `th` - ไทย

## Dépendances

- Apache HttpClient 5 - Client HTTP
- Gson - Parsing JSON
- SLF4J + Logback - Logging

## Exécution de l'exemple

**PowerShell (Windows):**

```powershell
mvn clean compile exec:java '-Dexec.mainClass=net.tcgdex.App'
```

**Bash/Terminal (Linux/Mac):**

```bash
mvn clean compile exec:java -Dexec.mainClass=net.tcgdex.App
```

Ou empaqueter en JAR :

```bash
mvn clean package
java -jar target/pokemon-tcg-client-1.0.0.jar
```

## Gestion des Erreurs

Les méthodes lancent des `IOException` en cas d'erreur réseau. Les erreurs sont loggées via SLF4J.

```java
try {
    Card card = client.getCardService().getCard("base4-1");
} catch (IOException e) {
    logger.error("Erreur d'accès à l'API", e);
}
```

## Ressources

- [Documentation API TCGdex](https://tcgdex.dev)
- [Référence API REST](https://tcgdex.dev/rest)
- [Source du SDK Java](https://github.com/tcgdex/java-sdk)

## Licence

MIT

## Contributeurs

Fait avec ❤️ pour la communauté Pokémon TCG
