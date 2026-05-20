# Mise en ligne

## Recommandation

Pour ce projet, le chemin le plus simple est :

1. heberger l'application sur Railway ou Render
2. utiliser PostgreSQL en production
3. garder H2 uniquement pour le developpement local

Le projet est deja pret pour ce schema :

- profil local par defaut : `dev`
- profil cloud : `prod`
- image Docker fournie
- endpoint de sante disponible sur `/actuator/health`
- connexion PostgreSQL possible via `SPRING_DATASOURCE_URL` ou variables `PG*`

## Variables de production

Configurer au minimum :

- `SPRING_PROFILES_ACTIVE=prod`
- `PORT=9012` ou laisser la plateforme injecter sa variable
- `SPRING_DATASOURCE_URL=jdbc:postgresql://...`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=...`

Optionnel selon la plateforme :

- `PGHOST=...`
- `PGPORT=...`
- `PGDATABASE=...`
- `PGUSER=...`
- `PGPASSWORD=...`

## Railway

### Application

1. pousser le repo sur GitHub
2. creer un projet Railway
3. deployer le repo GitHub
4. generer un domaine public
5. definir le healthcheck sur `/actuator/health`

### Base PostgreSQL

1. ajouter un service PostgreSQL au projet
2. relier l'application a la variable de base :
   `SPRING_DATASOURCE_URL=${{Postgres.DATABASE_URL}}`
3. definir aussi :
   `SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}`
   `SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}`
4. garder `SPRING_PROFILES_ACTIVE=prod`
5. redeployer l'application

## Render

### Application

1. pousser le repo sur GitHub
2. creer un Web Service Render depuis le repo
3. utiliser le `Dockerfile` du projet
4. definir `SPRING_PROFILES_ACTIVE=prod`
5. definir le healthcheck sur `/actuator/health`

### Base PostgreSQL

1. creer une base Render Postgres
2. recuperer l'URL interne PostgreSQL
3. definir `SPRING_DATASOURCE_URL` sur le Web Service
4. definir `SPRING_DATASOURCE_USERNAME`
5. definir `SPRING_DATASOURCE_PASSWORD`
6. redeployer

## Securite minimale deja prevue

- mot de passe utilisateur chiffre avec BCrypt
- console H2 desactivee par defaut
- cookie de session `HttpOnly`
- `SameSite=Lax`
- cookie `Secure` en profil `prod`
- prise en charge des proxies HTTPS avec `server.forward-headers-strategy=framework`
- protection CSRF active sur les formulaires et actions d'ecriture

## Avant la premiere mise en ligne

1. creer un premier compte admin/utilisateur apres le deploiement
2. verifier que la connexion fonctionne en HTTPS
3. verifier que `h2-console` est inaccessible en production
4. verifier que `/actuator/health` repond bien
5. verifier que la collection reste bien persistante apres un redemarrage
