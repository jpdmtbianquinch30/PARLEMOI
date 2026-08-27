# PARLE-MOI — Backend

Bilan de l'état actuel du backend Spring Boot du projet PARLE-MOI (plateforme d'écoute anonyme, Sénégal).

Dernière mise à jour : 26 août 2026 (après test général de bout en bout)

---

## 1. Stack technique

| Couche | Techno | Version |
|---|---|---|
| Langage | Java | 17 |
| Framework | Spring Boot | 4.1.0 |
| Spring Framework (sous-jacent) | Spring Framework | 7.0.8 |
| Sécurité | Spring Security | 7.1.0 |
| Persistance | Spring Data JPA / Hibernate | 7.4.1.Final |
| Base de données | PostgreSQL | 16 |
| Pool de connexions | HikariCP | 7.0.2 |
| Build | Maven | 3.9.11 |
| Conteneurisation (DB dev) | Docker Compose | v2.40.2 |
| Génération de code | Lombok | — |

> ⚠️ **Point important** : le projet est parti sur **Spring Boot 4.x** (et non 3.x comme initialement prévu) car Spring Initializr n'accepte plus que la branche 4.x depuis fin juin 2026 (fin de vie officielle de Spring Boot 3.5 le 30 juin 2026). Cela implique Jakarta EE 11, Jackson 3 (package `tools.jackson.*`), et un système de sécurité (`PathPatternRequestMatcher`) différent de la documentation Spring Boot 3.x qu'on pourrait trouver en ligne.

---

## 2. Structure du projet

```
PARLEMOI/
├── docker-compose.yml              # PostgreSQL pour le dev local
└── parle-moi-backend/
    └── src/main/java/sn/parlemoi/backend/
        ├── entity/                 # Entités JPA
        ├── enums/                  # Enums métier
        ├── repository/             # Repositories Spring Data JPA
        ├── service/                # Logique métier
        ├── controller/             # Endpoints REST
        ├── dto/                    # Objets de transfert (jamais d'entité exposée directement)
        ├── exception/               # Exceptions métier + gestion centralisée
        └── config/                 # Configuration (sécurité, DataSeeder dev, etc.)
```

---

## 3. Base de données — accès dev local

```
Host      : localhost
Port      : 5432
Database  : parlemoi
User      : parlemoi_user
Password  : parlemoi_dev_password
```

⚠️ Mot de passe de développement uniquement. À remplacer par un secret fort en variables d'environnement avant tout déploiement.

Démarrage :
```powershell
cd C:\Users\jeanp\Dev\Projects\java\PARLEMOI
docker compose up -d
```

---

## 4. Modèle de données — entités créées

| Entité | Rôle | Points clés |
|---|---|---|
| `Ecoutant` | L'écoutante (unique en V1, modèle évolutif) | Statut `enLigne` manuel, horaires fixes distincts du statut temps réel, durée de rétention des messages configurable (`H24`/`J7`/`J30`) |
| `Utilisateur` | Ancrage technique anonyme | Aucune donnée identifiante stockée (pas de nom/email/téléphone) |
| `Conversation` | Cœur du chat (1 code = 1 conversation) | `code` public distinct de l'`id` technique, `positionFileAttente` nullable |
| `Message` | Messages du chat | `expireLe` calculé selon la rétention configurée par l'écoutante |
| `Appel` | Appel audio WebRTC | `startedAt` = moment réel du décroché, déclenche le décompte du forfait |
| `Service` | Catégorie de service (catalogue) | ⚠️ Nom en conflit potentiel avec `@Service` Spring — vigilance sur les imports |
| `Formule` | Offre tarifaire liée à un service | `prix` en `BigDecimal` (jamais `double`/`float`) |
| `Reservation` | Réservation Phase 1 (MVP) | Contrainte unique SQL `(date, heure)` en plus du check applicatif — double protection anti double-booking |

**Règle appliquée à toutes les entités** : `id` technique en UUID (non énumérable), séparé de tout `code` public court communiqué à l'utilisateur.

---

## 5. Sécurité (SecurityConfig)

- API **stateless** (pas de session serveur, CSRF désactivé consciemment — justifié car pas de cookies de session à protéger)
- Routes publiques (`permitAll`) : `/api/public/**`, `/api/reservations/**`, `/api/creneaux/**`, `/ws/**`, `/error`
- Routes admin (`authenticated`) : `/api/admin/**` — **authentification réelle (JWT) pas encore implémentée**, à faire en Phase 2
- Tout le reste bloqué par défaut (principe du moindre privilège)
- CORS restreint à `http://localhost:4200` (Angular dev) — **à changer en production**
- Mots de passe hashés via `BCryptPasswordEncoder` (bean prêt, pas encore utilisé — attend l'auth admin)

> 📌 **Piège rencontré et corrigé** : Spring Security 7 a supprimé `AntPathRequestMatcher`/`MvcRequestMatcher`. Tous les matchers du projet utilisent désormais `PathPatternRequestMatcher.withDefaults()`, seule méthode fiable en Spring Boot 4.1.
>
> 📌 **Piège rencontré et corrigé** : les dispatches internes vers `/error` repassent par le filtre de sécurité — `/error` doit rester explicitement en `permitAll`, sinon toute page d'erreur standard renvoie un 403 au lieu du code réel.

---

## 6. Endpoints REST disponibles

| Méthode | Route | Description | Statut |
|---|---|---|---|
| `POST` | `/api/reservations` | Créer une réservation | ✅ Testé de bout en bout |
| `GET` | `/api/reservations/{code}` | Retrouver une réservation par code | ✅ Testé de bout en bout |
| `POST` | `/api/reservations/{code}/annuler` | Annuler une réservation | ✅ Testé de bout en bout |

⚠️ **Pas encore de rate limiting** sur `GET /api/reservations/{code}` — nécessaire avant mise en production pour empêcher l'énumération de codes par force brute (mentionné dès le briefing initial).

### Données de test (dev uniquement)

Un `DataSeeder` (`config/DataSeeder.java`) injecte automatiquement un `Service` + 2 `Formule` au premier démarrage, uniquement si la base est vide (`count() == 0`, jamais de duplication). Actif via `@Profile("!prod")` — **ne s'exécute jamais si un profil `prod` est activé**. Les UUID générés s'affichent dans les logs de démarrage.

---

## 7. Gestion des erreurs (GlobalExceptionHandler)

| Exception | Code HTTP | Notes |
|---|---|---|
| `RessourceNonTrouveeException` | 404 | Ressource/formule/réservation introuvable |
| `ReservationConflitException` | 409 | Créneau déjà pris (double-booking) |
| `MethodArgumentNotValidException` | 400 | Validation `@Valid` échouée, détail par champ |
| `HttpMessageNotReadableException` | 400 | JSON malformé côté client |
| `Exception` (générique) | 500 | Message générique au client, **stack trace complète toujours loguée côté serveur** (jamais d'erreur silencieuse) |

Toute donnée sensible (détails internes, stack traces) reste côté serveur — jamais renvoyée au client en clair.

---

## 7bis. Test général de bout en bout — résultats

Une batterie de 9 tests a été exécutée contre le flow de réservation complet (création, conflit, validation, recherche, annulation), avec PostgreSQL réel et données injectées par le `DataSeeder`.

| # | Scénario | Résultat attendu | Résultat obtenu |
|---|---|---|---|
| 1 | Création valide | 201 | ✅ 201 — code généré (ex: `PM-KKV5ZR`) |
| 2 | Double-booking (même créneau) | 409 | ✅ 409 "Ce créneau n'est plus disponible" |
| 3 | Formule inexistante | 404 | ✅ 404 "Formule introuvable ou inactive" |
| 4 | Date dans le passé | 400 | ✅ 400 — validation `@Future` déclenchée |
| 5 | Champ obligatoire manquant | 400 | ✅ 400 — validation `@NotBlank` déclenchée |
| 6 | Recherche par code valide | 200 | 🔧 500 au premier passage → **bug trouvé et corrigé** → ✅ 200 |
| 7 | Recherche par code inexistant | 404 | ✅ 404 "Aucune réservation trouvée" |
| 8 | Annulation réussie | 200, statut `ANNULEE` | ✅ 200 |
| 9 | Double annulation | 409 | ✅ 409 "Déjà annulée" |

**Verdict : 9/9 après correction.** Le flow de réservation Phase 1 est fiable en conditions réelles (PostgreSQL, contraintes SQL, validations, gestion d'erreurs).

### Bug trouvé et corrigé pendant ce test

**Symptôme** : `GET /api/reservations/{code}` renvoyait un `500` au lieu d'un `200`, alors que la même logique de lecture fonctionnait dans `annulerParCode`.

**Cause réelle** : `trouverParCode()` n'était pas annotée `@Transactional`. Avec `spring.jpa.open-in-view=false` (configuré volontairement dès l'étape 3), la session Hibernate se ferme dès que le repository retourne. Or `Reservation.formule` est chargée en `FetchType.LAZY`, et `versReponse()` accède à `reservation.getFormule().getNom()` — sans transaction active à ce moment, Hibernate ne peut plus charger la relation, ce qui lève une exception convertie en `500` par le handler générique.

**Correctif** : ajout de `@Transactional` sur `trouverParCode()` dans `ReservationService`.

**Règle à appliquer partout dans le projet à partir de maintenant** : toute méthode de service qui lit une entité avec une relation `FetchType.LAZY` puis accède à cette relation doit être annotée `@Transactional` (ou `@Transactional(readOnly = true)` pour les lectures pures), même si elle n'écrit rien en base. Point de vigilance particulier à venir pour `Conversation`, `Message` et `Appel`, qui ont le même type de relations.

**Pourquoi ce bug n'était pas resté silencieux** : grâce à l'ajout préalable de `log.error(...)` dans le `GlobalExceptionHandler` (avant cette étape, les erreurs `500` n'étaient pas loguées côté serveur, seulement masquées côté client) — sans ce logging, ce bug serait passé totalement inaperçu, y compris en production.

---

## 8. Décisions techniques structurantes à retenir

1. **UUID partout pour les id techniques**, jamais d'ID auto-incrémenté exposé — anti-énumération.
2. **Codes publics courts et lisibles** (`PM-7K4X92`), générés via `SecureRandom` sur un alphabet sans caractères ambigus (`0/O`, `1/I/L` exclus), 887 millions de combinaisons possibles.
3. **`BigDecimal` obligatoire pour toute donnée monétaire**, jamais `double`/`float`.
4. **Anonymat structurel** : l'entité `Utilisateur` ne contient aucune donnée identifiante par conception.
5. **Double protection anti-concurrence** : vérification applicative rapide + contrainte SQL unique en filet de sécurité, avec conversion propre de `DataIntegrityViolationException`.
6. **Jamais d'entité JPA exposée directement dans l'API** — toujours des DTO (`record` Java) dédiés en entrée/sortie.
7. **Sous PowerShell, `Invoke-RestMethod`/`Invoke-WebRequest` obligatoires pour les tests d'API** — `curl.exe` avec JSON inline échoue systématiquement à cause de l'échappement des guillemets par PowerShell.
8. **`@Transactional` obligatoire sur toute méthode de service accédant à une relation `FetchType.LAZY`**, même en lecture pure — sinon `LazyInitializationException` masquée en `500` à cause de `open-in-view=false` (bug réel rencontré et corrigé, voir section 7bis).
9. **Toute erreur `500` doit être loguée intégralement côté serveur** (`log.error`) même quand le message renvoyé au client reste générique — sans quoi les bugs réels passent inaperçus, y compris en production.

---

## 9. Prochaines étapes (non commencées)

- [x] ~~Peuplement de données de test (`Service`/`Formule`)~~ — fait via `DataSeeder`
- [x] ~~Test de bout en bout du flow de réservation~~ — 9/9, voir section 7bis
- [ ] Endpoint `GET /api/creneaux` — disponibilités en temps réel
- [ ] Rate limiting sur la recherche par code
- [ ] Authentification admin réelle (JWT) sur `/api/admin/**`
- [ ] Dashboard admin (statistiques, calendrier, gestion des créneaux/services/formules)
- [ ] WebSocket (STOMP) pour le chat temps réel — ⚠️ appliquer `@Transactional` sur les lectures `Conversation`/`Message`/`Appel` dès le départ (voir bug section 7bis)
- [ ] Signaling WebRTC + serveur TURN (Coturn) pour les appels audio
- [ ] Job planifié (`@Scheduled`) de purge des messages/conversations selon la rétention configurée
- [ ] Intégration paiement (Wave / Orange Money)
- [ ] Frontend Angular (standalone components)
- [ ] Dockerisation complète (backend + frontend + DB) et déploiement VPS avec HTTPS

---

## 10. Commandes utiles

```powershell
# Lancer la base de données
cd C:\Users\jeanp\Dev\Projects\java\PARLEMOI
docker compose up -d

# Compiler le backend
cd parle-moi-backend
mvn compile

# Lancer le backend
mvn spring-boot:run

# Tester un endpoint (PowerShell natif, pas curl.exe)
$body = @{
    formuleId = "xxx"
    dateReservation = "2026-09-01"
    heureReservation = "14:00:00"
    sujetOptionnel = "Test"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/reservations" -Method Post -ContentType "application/json" -Body $body
```