# 📖 READ ME FIRST!

Bienvenue! Voici votre client Pokemon TCG pour Java.

## ⏱️ Par où commencer?

### Option A: Je veux juste l'essayer (2 min)

1. Ouvrez PowerShell
2. Allez au dossier: `cd "c:\Users\flori\OneDrive\Bureau\Projet api pkmn"`
3. Tapez: `mvn exec:java`
4. Regardez les cartes Pokémon s'afficher! 🎴

### Option B: Je veux comprendre (10 min)

1. Lisez: **[WELCOME.md](WELCOME.md)** - Introduction générale
2. Lisez: **[QUICKSTART.md](QUICKSTART.md)** - Démarrage rapide
3. Exécutez l'exemple (Option A)

### Option C: Je veux tout savoir (30+ min)

1. Lisez: **[INDEX.md](INDEX.md)** - Guide complet de navigation
2. Consultez la documentation selon vos besoins

---

## 📚 Documentation complète (10 fichiers)

| Fichier                                      | Pour qui?              | Temps  |
| -------------------------------------------- | ---------------------- | ------ |
| [WELCOME.md](WELCOME.md)                     | Tour d'horizon         | 3 min  |
| [QUICKSTART.md](QUICKSTART.md)               | Installation + exemple | 5 min  |
| [README.md](README.md)                       | Référence complète     | 10 min |
| [ADVANCED_USAGE.md](ADVANCED_USAGE.md)       | Exemples avancés       | 15 min |
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | Architecture détaillée | 20 min |
| [INDEX.md](INDEX.md)                         | Navigation guide       | 5 min  |
| [COMMANDS.md](COMMANDS.md)                   | Commandes Maven        | 5 min  |
| [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)     | Statistiques projet    | 5 min  |
| [SETUP_COMPLETE.md](SETUP_COMPLETE.md)       | Installation ok        | 2 min  |
| [CHANGELOG.md](CHANGELOG.md)                 | Historique versions    | 2 min  |

---

## 🚀 Commandes essentielles

```bash
# 1. Compiler le projet
mvn clean compile

# 2. Exécuter l'exemple
mvn exec:java

# 3. Lancer les tests
mvn test

# 4. Créer un JAR
mvn clean package

# Voir toutes les commandes dans: COMMANDS.md
```

---

## 💡 Exemple simple

```java
import net.tcgdex.TCGdexClient;

public class Main {
    public static void main(String[] args) throws Exception {
        // Créer un client
        TCGdexClient client = new TCGdexClient("en");

        // Chercher des cartes
        List<CardBrief> pikachus = client.getCardService()
            .searchByName("Pikachu");

        // Afficher
        pikachus.forEach(p ->
            System.out.println(p.getName())
        );
    }
}
```

---

## ❓ J'ai une question...

### "Par où commencer?"

→ Lisez **[WELCOME.md](WELCOME.md)** (3 min)

### "Comment faire X?"

→ Allez dans **[INDEX.md](INDEX.md)** et cherchez
→ Ou lisez **[ADVANCED_USAGE.md](ADVANCED_USAGE.md)**

### "Erreur de compilation"

→ Consultez **[QUICKSTART.md](QUICKSTART.md#dépannage)**

### "Qu'est-ce qui a été créé?"

→ Lisez **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)**

### "Comment les commandes Maven?"

→ Consultez **[COMMANDS.md](COMMANDS.md)**

### "Architecture du projet?"

→ Lisez **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)**

---

## 📂 Structure

```
Projet api pkmn/
├── 📚 Documentation (10 fichiers .md)
│   └── Lisez celui qui correspond à votre besoin
├── 🔧 Code (src/)
│   ├── Java source (3 dossiers + 1 exemple)
│   └── Tests (2 fichiers)
├── 📦 Config
│   └── pom.xml (Maven configuration)
└── 🎁 Build output (target/)
    └── JAR compilé & prêt à l'emploi
```

---

## ✨ Ce qui a été créé

✅ **Client Java complet** pour l'API TCGdex
✅ **47 fichiers** incluant code + doc
✅ **12 classes Java** bien structurées
✅ **10 fichiers de documentation** détaillée
✅ **Tests unitaires** inclus
✅ **JAR compilé** prêt à l'emploi
✅ **Exemple fonctionnel** que vous pouvez exécuter

---

## 🎯 Racine recommandée de lecture

```
1️⃣  WELCOME.md           → Qu'est-ce que c'est?
2️⃣  QUICKSTART.md        → Comment commencer?
3️⃣  README.md            → Tous les détails
4️⃣  ADVANCED_USAGE.md    → Cas d'usage avancés
5️⃣  PROJECT_STRUCTURE.md → Comment ça marche?
6️⃣  INDEX.md             → Trouver n'importe quoi
```

---

## 🎴 Fonctionnalités principales

- 🎴 Accédez aux cartes Pokémon TCG
- 📦 Explorez les ensembles (sets)
- 🌍 Support 10+ langues
- 🔍 Recherche et filtrage
- 🧪 Tests inclus
- 📚 Doc complète

---

## 🔗 Ressources

- 🌐 TCGdex: https://tcgdex.dev
- 📖 API Docs: https://tcgdex.dev/rest
- 💬 Discord: https://tcgdex.dev/discord
- 🐛 GitHub: https://github.com/tcgdex

---

## ✅ Vous êtes prêt!

**Maintenant:**

1. Ouvrez PowerShell
2. Allez au dossier du projet
3. Exécutez: `mvn exec:java -Dexec.mainClass="net.tcgdex.App"`
4. Voyez les cartes Pokémon! 🎴

### ⚠️ Note PowerShell Windows

Sous Windows PowerShell, la commande est simplement:

```powershell
mvn exec:java
```

**Ou lisez d'abord: [WELCOME.md](WELCOME.md)**

---

**Créé le**: 8 avril 2026  
**Version**: 1.0.0  
**Status**: ✅ Production Ready

```
🎴 Bienvenue! Prêt à explorer? 🎴
```
