# PocketBudget

PocketBudget est une application Android native en Kotlin et XML Views pour suivre ses revenus, dépenses, catégories et budgets mensuels.

## Fonctionnalités

- Tableau de bord : solde, revenus, dépenses et transactions récentes.
- Transactions : ajout, modification et suppression avec validation de montant.
- Catégories : CRUD de catégories de revenus et de dépenses.
- Budgets : limites mensuelles par catégorie, suivi des dépenses et alerte de dépassement.
- Recherche dans les notes ou catégories, filtre de catégorie et tri par date ou montant.
- Import initial de 20 transactions JSON, une seule fois, sans écraser les données existantes.
- Préférences DataStore : devise EUR/USD, affichage des revenus et ordre de tri.

## Architecture

```text
Activities / XML Views
        │
        ▼
ViewModel ── Flow<UserSettings>
        │
        ▼
DashboardRepository ── SettingsRepository
        │                       │
        ▼                       ▼
Room (DAO / Database)       DataStore Preferences
        │
        ▼
Transaction · Category · Budget
```

Les montants sont stockés en centimes d’euro pour éviter les erreurs d’arrondi. Le choix USD applique un taux fixe de démonstration dans l’interface.

## Lancer le projet

1. Ouvrir le dossier racine dans Android Studio.
2. Choisir le JDK 17 fourni par Android Studio et synchroniser Gradle.
3. Démarrer un émulateur API 35.
4. Lancer la configuration `app`.

Pour charger les 20 transactions de démonstration après un essai précédent, effacer les données de PocketBudget dans les réglages Android puis relancer l’application.

## Tests

Les tests locaux couvrent le formatage de devise, la recherche, le filtrage, le masquage des revenus et le tri.

```sh
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

Les tests instrumentés vérifient les actions principales du tableau de bord et l’écran Settings sur émulateur.
