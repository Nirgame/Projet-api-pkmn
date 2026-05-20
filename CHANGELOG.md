# Changelog

Tous les changements notables à ce projet seront documentés dans ce fichier.

## [1.0.0] - 2026-04-08

### Ajouté

- Client Java pour l'API TCGdex
- Services pour accéder aux cartes, ensembles et séries
- Support multilingue (10+ langues)
- Utilitaire HTTP client pour les requêtes API
- Modèles de données pour Card, CardBrief, Set et Serie
- Gestion complète des erreurs
- Système de logging avec SLF4J/Logback
- Tests unitaires basiques
- Documentation complète (README.md, ADVANCED_USAGE.md)
- Exemple d'utilisation (App.java)

### Fonctionnalités

- Récupération des cartes Pokémon
- Filtrage et recherche des cartes
- Gestion des ensembles (sets)
- Accès aux séries
- Support du multilingue
- Architecture modulaire et extensible

### Dépendances

- Apache HttpClient 5.2.1
- Gson 2.10.1
- SLF4J 2.0.7 + Logback 1.4.11
- JUnit 4.13.2 (tests)

## [À venir]

- Cache intégré
- Pagination automatique
- Support GraphQL
- Serialization/Deserialization personnalisée
- Webhooks
- Proxy support
- Rate limiting
