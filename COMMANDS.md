# ⚡ Commandes rapides

Fichier de référence pour les commandes les plus utilisées.

## 🔧 Compilation et Build

### Nettoyer + Compiler

```bash
mvn clean compile
```

### Compiler sans tester

```bash
mvn compile -DskipTests
```

### Build complet (JAR)

```bash
mvn clean package
```

### Build avec shadowing (uber JAR)

```bash
mvn clean package shade:shade
```

---

## 🧪 Tests

### Lancer tous les tests

```bash
mvn test
```

### Tests d'une classe spécifique

```bash
mvn test -Dtest=CardServiceTest
```

### Skip les tests

```bash
mvn compile -DskipTests
```

### Couverage des tests

```bash
mvn test jacoco:report
```

---

## ▶️ Exécution

### Exécuter l'application exemple

**Toutes les plateformes:**

```bash
mvn exec:java
```

### Exécuter avec classe spécifique

Si vous voulez spécifier une autre classe principale:

**PowerShell (Windows):**

```powershell
mvn exec:java '-Dexec.mainClass=com.example.MyClass'
```

**Bash/Terminal (Linux/Mac):**

```bash
mvn exec:java -Dexec.mainClass=com.example.MyClass
```

### Exécuter le JAR

```bash
java -jar target/pokemon-tcg-client-1.0.0.jar
```

### Exécuter avec options

```bash
java -Dcom.sun.management.jmxremote -jar target/pokemon-tcg-client-1.0.0.jar
```

---

## 📚 Documentation

### Générer JavaDoc

```bash
mvn javadoc:javadoc
```

### Voir la JavaDoc

```bash
mvn javadoc:javadoc site:run
```

---

## 🔍 Inspection

### Dependency tree

```bash
mvn dependency:tree
```

### Effective POM

```bash
mvn help:effective-pom
```

### Project info

```bash
mvn project-info-reports:dependencies
```

---

## 📦 Gestion des dépendances

### Mettre à jour les dépendances

```bash
mvn versions:display-dependency-updates
```

### Checker les dépendances expirees

```bash
mvn versions:display-property-updates
```

---

## 🚀 Déploiement

### Installer localement

```bash
mvn clean install
```

### Installer sans tester

```bash
mvn clean install -DskipTests
```

---

## 🐛 Débogage

### Afficher les debug logs

```bash
mvn clean compile -X
```

### Verbose mode

```bash
mvn clean compile -V
```

---

## ⚙️ Configuration

### Vérifier la version Java

```bash
java -version
```

### Vérifier Maven

```bash
mvn -version
```

### M2 Repository location

```bash
mvn help:describe -Dplugin=org.apache.maven.plugins:maven-compiler-plugin
```

---

## 🎯 Workflows complets

### Développement local

```bash
mvn clean compile exec:java '-Dexec.mainClass=net.tcgdex.App'
```

### Avant de commiter

```bash
mvn clean test
```

### Préparer pour production

```bash
mvn clean package -DskipTests
```

### Déploiement complet

```bash
mvn clean install deploy
```

---

## 💻 IDE Integration

### Eclipse

```bash
mvn eclipse:eclipse
mvn eclipse:clean
```

### IntelliJ IDEA

Importez simplement le `pom.xml`

### VS Code

Installez l'extension Maven

---

## 🐚 PowerShell spécifique (Windows)

### Naviguer au projet

```powershell
cd "c:\Users\flori\OneDrive\Bureau\Projet api pkmn"
```

### Voir les fichiers

```powershell
Get-ChildItem -Recurse | Where-Object {$_.Extension -eq '.java'}
```

### Compiler

```powershell
mvn clean compile
```

### Exécuter

```powershell
mvn exec:java -Dexec.mainClass="net.tcgdex.App"
```

### Supprimez les builds

```powershell
Remove-Item -Recurse target/
Remove-Item -Recurse *.class
```

---

## 📝 Documentation

| Commande                         | Résultat             |
| -------------------------------- | -------------------- |
| `mvn site`                       | Génère le site Maven |
| `mvn javadoc:javadoc`            | Génère la JavaDoc    |
| `mvn project-info-reports:index` | Rapport projet       |

---

## 🔗 Ressources rapides

| Ressource     | URL                                                 |
| ------------- | --------------------------------------------------- |
| Maven Central | https://central.sonatype.com/                       |
| TC Gdex API   | https://tcgdex.dev/rest                             |
| Java Docs     | https://docs.oracle.com/en/java/javase/11/docs/api/ |
| Maven Docs    | https://maven.apache.org/guides/introduction/       |

---

## ✨ Alias utiles (à ajouter dans environment)

```bash
# Windows PowerShell Profile
function mvnclean { mvn clean }
function mvncompile { mvn clean compile }
function mvntest { mvn test }
function mvnbuild { mvn clean package }
function mvnrun { mvn exec:java '-Dexec.mainClass=net.tcgdex.App' }
```

---

**Version du fichier**: 1.0
**Dernière mise à jour**: 8 avril 2026
