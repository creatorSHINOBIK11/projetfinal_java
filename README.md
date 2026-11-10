# Hotel Management System (Projet Java POO)

Système de gestion hôtelière en mode console développé en Java (Java 21 / Maven / JUnit 5).
Ce projet permet de charger des données hôtelières (chambres, clients, réservations) depuis des fichiers CSV, de rechercher les disponibilités sans conflit de calendrier, de calculer des devis dynamiques selon différentes règles de tarification, et de piloter les réservations (création, annulation avec frais, check-in, check-out) et d'obtenir des statistiques de gestion (chiffre d'affaires et taux d'occupation).

---

## 1. Comment exécuter le projet

### Prérequis
- Java 21 (ou supérieur)
- Maven 3.8+ (ou utiliser le wrapper/Maven intégré à IntelliJ IDEA)

### Compilation et exécution
Pour compiler et empaqueter l'application :
```bash
mvn clean package
```

Pour lancer l'application en mode console :
```bash
java -jar target/hotel-management.jar
```
*(Vous pouvez également passer en argument un dossier contenant vos propres fichiers CSV : `java -jar target/hotel-management.jar ./mon_dossier_csv`)*.

---

## 2. Fonctionnalités principales (Main Features)

1. **Chargement automatique des données** depuis des fichiers CSV au démarrage (`rooms.csv`, `guests.csv`, `reservations.csv`).
2. **Consultation des catalogues** : visualisation des chambres (étage, type, capacité, équipements, prix) et des fiches clients.
3. **Recherche de chambres disponibles** : filtrage par capacité et détection automatique des conflits de dates sur les réservations actives.
4. **Moteur de tarification flexible** :
   - Tarifs selon le type de chambre (Standard, Deluxe avec petit-déjeuner par personne, Suite avec supplément par personne additionnelle).
   - Remises fidélité (Silver -5%, Gold -10%).
   - Remises long séjour (-10% dès 7 nuits).
   - Majorations haute saison (+20% sur les nuits en juillet/août).
5. **Gestion du cycle de vie des réservations** :
   - Création avec devis détaillé et validation de l'historique de disponibilité.
   - Politique d'annulation (gratuite à plus de 48h de l'arrivée, 50% de frais si annulation tardive).
   - Check-in et Check-out avec transitions d'états contrôlées.
6. **Rapports de gestion** : calcul du chiffre d'affaires total (incluant les frais d'annulation) et calcul du taux d'occupation quotidien ou moyen.

---

## 3. Architecture et Classes Principales

- **`com.hotel.model`** :
  - `Room` (classe abstraite) : définit les attributs communs (`number`, `floor`, `basePrice`, `capacity`, `amenities`) et impose la méthode de calcul du tarif à ses sous-classes.
  - `StandardRoom`, `DeluxeRoom`, `Suite` (classes concrètes) : implémentent leur logique tarifaire spécifique.
  - `Guest` : représente un client et son niveau de fidélité (`LoyaltyLevel`).
  - `Reservation` : agrège un client, une chambre, une période (`DateRange`) et gère les statuts (`CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`).
  - `DateRange` (record) : encapsule les dates d'arrivée et départ, fournit les méthodes `nights()`, `overlaps()` et `nightDates()`.
- **`com.hotel.pricing`** :
  - `PricingRule` (interface) : contrat pour chaque règle de majoration ou remise.
  - `PriceCalculator` : applique la liste des règles de manière découplée.
- **`com.hotel.repository`** :
  - `RoomRepository`, `GuestRepository`, `ReservationRepository` (interfaces) et leurs implémentations `InMemory...Repository`.
- **`com.hotel.service`** :
  - `AvailabilityService` : vérifie la liberté des chambres et l'absence de chevauchement.
  - `ReservationService` : orchestre les opérations métier de réservation.
  - `CancellationPolicy` : calcule les pénalités d'annulation.
  - `ReportService` : statistiques et chiffre d'affaires.
- **`com.hotel.loader`** :
  - `CsvReader`, `RoomCsvLoader`, `GuestCsvLoader`, `ReservationCsvLoader`.
- **`com.hotel.ui`** :
  - `ConsoleMenu` : interface utilisateur en ligne de commande.

---

## 4. Choix techniques et Justifications (Technical Decisions)

1. **Utilisation d'un `record DateRange` immuable avec validation stricte** :
   - *Raison* : Assure qu'une date de départ est toujours postérieure à l'arrivée et rend la détection de conflits (`overlaps`) centralisée et sans effet de bord.
2. **Utilisation de `BigDecimal` pour tous les montants financiers (`Money`)** :
   - *Raison* : Les types primitifs à virgule flottante (`float`, `double`) introduisent des erreurs d'arrondi binaires inacceptables dans la gestion de devis et de remises en pourcentage.
3. **Architecture en couches découplées par interfaces (Repository & Rules)** :
   - *Raison* : Isole totalement la logique métier du mode de stockage et permet des tests unitaires ultra-rapides et déterministes.
4. **Injection d'un `java.time.Clock` dans `ReservationService`** :
   - *Raison* : Permet de tester sans faille les règles basées sur la date courante (comme l'annulation tardive ou le refus des dates passées) sans dépendre de l'horloge système du poste.

---

## 5. Utilisation de l'Héritage (Inheritance)

L'héritage est mis en œuvre au niveau de la hiérarchie des chambres :
```
           [Room] (abstract)
          /   |   \
         /    |    \
[StandardRoom] [DeluxeRoom] [Suite]
```
- **Classe mère `Room`** : regroupe le numéro, l'étage, le prix de base, la capacité et la gestion des équipements (`Amenity`).
- **Sous-classes concrètes** :
  - `StandardRoom` hérite directement et applique le tarif fixe de base.
  - `DeluxeRoom` hérite de `Room` et intègre un coût de petit-déjeuner par personne.
  - `Suite` hérite de `Room` et ajoute un supplément pour les convives au-delà de deux personnes.

---

## 6. Utilisation du Polymorphisme (Polymorphism)

Le polymorphisme est exploité à deux niveaux majeurs :
1. **Polymorphisme sur les chambres** :
   Dans `AvailabilityService` ou lors de la valorisation d'un devis :
   ```java
   Room room = ...; // peut être Standard, Deluxe ou Suite
   BigDecimal price = room.calculateNightlyPrice(guestCount);
   ```
   Le comportement varie dynamiquement selon l'instance concrète sans aucun `if-else` ou `instanceof`.
2. **Polymorphisme sur les règles tarifaires (`PricingRule`)** :
   Le `PriceCalculator` itère sur une collection `List<PricingRule>` :
   ```java
   for (PricingRule rule : rules) {
       BigDecimal adj = rule.adjustment(context);
       ...
   }
   ```
   Chaque règle (`LoyaltyDiscount`, `LongStayDiscount`, `HighSeasonSurcharge`) applique son propre calcul.

---

## 7. Utilisation de Classe Abstraite et d'Interface

- **Classe Abstraite `Room`** :
  - *Pourquoi une classe abstraite ?* Car toutes les chambres partagent un état interne (numéro, étage, prix de base, etc.) et des règles de validation invariantes (ex: capacité minimale), tout en imposant la définition de `priceForGuests(int)` par les sous-classes. Une chambre "générique" n'existe pas dans la réalité.
- **Interfaces (`PricingRule`, Repositories)** :
  - *Pourquoi des interfaces ?* Car elles définissent des contrats purs sans partage d'état. `PricingRule` permet d'ajouter n'importe quelle nouvelle logique de promotion sans toucher au code existant.

---

## 8. Exceptions Personnalisées (Custom Exceptions)

Le projet implémente une hiérarchie d'exceptions sous le package `com.hotel.exception` :
- `HotelException` : classe de base de toutes les exceptions métier de l'application.
- `RoomNotAvailableException` : levée lorsqu'une chambre demandée est déjà réservée sur la période voulue.
- `ReservationNotFoundException` : levée lorsqu'un identifiant de réservation est inconnu.
- `InvalidReservationException` : levée pour toute violation de règle métier (dates dans le passé, capacité dépassée, statut de réservation incorrect lors d'un check-in/out).
- `DataLoadingException` : levée en cas de dysfonctionnement bloquant lors de la lecture des fichiers CSV.

---

## 9. Chargement des Données (Data Loading)

Au lancement de l'application :
1. La classe `Application` invoque `RoomCsvLoader`, `GuestCsvLoader` et `ReservationCsvLoader`.
2. Ces loaders s'appuient sur `CsvReader` qui lit et analyse les fichiers CSV ligne par ligne en ignorant les commentaires (`#`) et les lignes vides.
3. Par défaut, les fichiers fournis dans `src/main/resources/data/` (`rooms.csv`, `guests.csv`, `reservations.csv`) sont chargés depuis le classpath, garantissant une exécution immédiate sans dépendance externe.

---

## 10. Application des Principes SOLID

- **S (Single Responsibility Principle)** :
  - `DateRange` s'occupe exclusivement de la cohérence des dates.
  - `CsvReader` et les loaders s'occupent uniquement de la désérialisation.
  - `ConsoleMenu` gère exclusivement les entrées/sorties utilisateur.
  - `Application` assemble les dépendances sans contenir de code métier.
- **O (Open/Closed Principle)** :
  - L'architecture de tarification est ouverte à l'extension : pour ajouter une nouvelle promotion (ex: remise Black Friday), il suffit de créer une nouvelle classe implémentant `PricingRule` sans modifier `PriceCalculator`.
- **L (Liskov Substitution Principle)** :
  - Toutes les sous-classes de `Room` respectent le contrat de la classe mère : la méthode `calculateNightlyPrice()` est finale et garantit que les bornes de capacité sont respectées uniformément.
- **I (Interface Segregation Principle)** :
  - Les interfaces de repository (`RoomRepository`, `GuestRepository`, `ReservationRepository`) sont spécialisées et n'exposent que les opérations nécessaires à leur domaine.
- **D (Dependency Inversion Principle)** :
  - Les services dépendent d'abstractions (interfaces `RoomRepository`, `PricingRule`), et non d'implémentations concrètes en mémoire ou fichier.

---

## 11. Comment exécuter les tests (How to Run the Tests)

Les tests unitaires ont été écrits avec **JUnit 5**. Ils couvrent les cas normaux, les cas limites (séjours consécutifs sans chevauchement) et les levées d'exceptions.

Pour lancer tous les tests unitaires :
```bash
mvn test
```
