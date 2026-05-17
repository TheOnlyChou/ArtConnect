# Étape 5 - Intégration complète, tests et documentation

## 1. Objectif de l’étape 5

L’objectif de cette étape était de finaliser l’intégration entre l’application JavaFX et la base de données MySQL.

Après avoir mis en place la structure du projet, les modèles Java, les scripts SQL et la persistance JDBC, cette dernière étape avait surtout pour but de vérifier que l’ensemble fonctionnait correctement en conditions réelles.

Concrètement, nous avons vérifié que :

- l’application se connecte correctement à la base de données ;
- les données affichées dans l’interface proviennent bien de MySQL ;
- les ajouts, modifications et suppressions réalisés depuis l’application sont bien enregistrés ;
- les données restent présentes après fermeture et relancement de l’application ;
- les principales fonctionnalités sont cohérentes avec les tables SQL et les modèles Java.

Cette étape permet donc de valider que l’application ne fonctionne pas seulement avec des données temporaires, mais qu’elle est bien reliée à une base de données persistante.

## 2. Architecture générale de l’application

L’application repose sur une architecture en plusieurs couches. Cela permet de séparer clairement l’interface graphique, la logique métier et l’accès à la base de données.

La chaîne générale de fonctionnement est la suivante :

```text
Interface JavaFX -> Controller -> Service -> DAO JDBC -> Base MySQL
````

Chaque couche a un rôle précis :

* les vues JavaFX affichent les données et récupèrent les actions de l’utilisateur ;
* les contrôleurs traitent les interactions avec l’interface ;
* les services contiennent la logique métier ;
* les DAO JDBC exécutent les requêtes SQL ;
* MySQL stocke réellement les données.

Cette organisation rend le projet plus lisible et plus facile à maintenir.

## 3. Connexion à la base de données

La connexion à la base est centralisée dans deux fichiers principaux :

* `DatabaseConfig.java`
* `ConnectionManager.java`

Le fichier `DatabaseConfig` contient les paramètres nécessaires à la connexion MySQL. Il peut utiliser des variables d’environnement, avec des valeurs par défaut si elles ne sont pas définies.

Exemple :

```java
String url = DatabaseConfig.URL;
String user = DatabaseConfig.USER;
String password = DatabaseConfig.PASSWORD;
```

Le fichier `ConnectionManager` permet ensuite de fournir des connexions JDBC aux DAO.

Exemple d’utilisation :

```java
try (Connection connection = ConnectionManager.getConnection();
     PreparedStatement statement = connection.prepareStatement(sql)) {

    statement.executeUpdate();

} catch (SQLException e) {
    throw new RuntimeException("Erreur base de données", e);
}
```

Cette structure évite de répéter la configuration de connexion dans plusieurs classes du projet.

## 4. Rôle du ServiceProvider

Le `ServiceProvider` permet de centraliser l’accès aux services utilisés par les contrôleurs.

Son rôle est important, car il permet de choisir les services utilisés par l’application. Quand la base de données est disponible, l’application utilise les services JDBC, ce qui permet d’enregistrer les données dans MySQL.

Exemple :

```java
private final ArtistService artistService = ServiceProvider.getArtistService();
```

Cela permet au contrôleur de travailler avec un service sans avoir besoin de connaître directement les détails de la base de données.

## 5. Exemple de fonctionnement complet

Voici un exemple simple avec la création d’un artiste.

Dans le contrôleur JavaFX, l’utilisateur remplit le formulaire puis clique sur un bouton. Le contrôleur crée alors un objet `Artist` et appelle le service :

```java
@FXML
private void handleCreate() {
    Artist artist = buildArtistFromForm();
    artistService.createArtist(artist);
    refreshTable();
}
```

Le service délègue ensuite l’enregistrement au DAO :

```java
@Override
public void createArtist(Artist artist) {
    artistDao.save(artist);
}
```

Le DAO exécute ensuite une requête SQL pour insérer l’artiste dans la base :

```java
String sql = "INSERT INTO Artist(id_artist, name_artist, birthYear_artist, contactEmail_artist, city_artist) VALUES (?, ?, ?, ?, ?)";
```

Ce fonctionnement montre bien le lien entre l’interface JavaFX et la base de données MySQL.

## 6. Scripts SQL utilisés

Pour lancer correctement le projet, les scripts SQL doivent être exécutés dans le bon ordre.

Les scripts principaux sont :

1. `tablesPrincipales.sql`
2. `insertionDonnees.sql`

Le premier script crée la base de données, les tables, les clés primaires, les clés étrangères et les principales contraintes.

Le second script insère les données de départ utilisées par l’application.

D’autres scripts peuvent aussi être utilisés :

* `triggers.sql`
* `vuesIndex.sql`
* `transactions.sql`

Ces scripts ne sont pas forcément obligatoires pour lancer l’application, mais ils permettent d’ajouter des validations, des vues, des index et certaines procédures utiles pour tester des cas plus avancés.

Ordre recommandé :

```sql
SOURCE ArtConnectBDD/tablesPrincipales.sql;
SOURCE ArtConnectBDD/insertionDonnees.sql;
SOURCE ArtConnectBDD/triggers.sql;
SOURCE ArtConnectBDD/vuesIndex.sql;
```

## 7. Correspondance entre les onglets et la base de données

Chaque onglet de l’application est relié à une ou plusieurs tables de la base.

| Onglet      | Tables principalement utilisées                         | Fonctionnalités                               |
| ----------- | ------------------------------------------------------- | --------------------------------------------- |
| Artists     | `Artist`, `Discipline`, `Artist_Discipline`             | Ajout, modification, suppression, recherche   |
| Artworks    | `Artwork`, `Artist`, `ArtworkTag`, `Artwork_ArtworkTag` | Ajout, modification, suppression, recherche   |
| Galleries   | `Gallery`                                               | Affichage des galeries                        |
| Exhibitions | `Exhibition`, `Gallery`, `Artwork_Exhibition`           | Affichage des expositions                     |
| Workshops   | `Workshop`, `Artist`                                    | Affichage des ateliers                        |
| Community   | `CommunityMember`, `CommunityMember_Discipline`         | Affichage des membres                         |
| Bookings    | `Booking`, `Workshop`, `CommunityMember`                | Gestion des réservations                      |
| Discover    | `Artwork`, `Workshop`, `Exhibition`                     | Affichage synthétique des données importantes |

Cette correspondance montre que les onglets ne sont pas indépendants de la base : les informations affichées proviennent bien des tables MySQL.

## 8. Tests réalisés

Les tests ont été réalisés directement dans l’application, puis vérifiés avec la base de données MySQL.

L’objectif était de vérifier que les principales fonctionnalités fonctionnaient correctement, mais aussi que les données étaient bien persistées après redémarrage de l’application.

### Test 1 - Lancement de l’application

Objectif : vérifier que l’application démarre correctement et se connecte à la base de données.

Résultat : l’application se lance sans erreur bloquante. Les données sont bien chargées dans les différents onglets.

Vérification possible :

```sql
SHOW TABLES FROM ArtConnect;
```

Ce test a été validé.

### Test 2 - Chargement des données initiales

Objectif : vérifier que les données insérées avec les scripts SQL apparaissent bien dans l’interface.

Résultat : les artistes, œuvres, galeries, expositions, ateliers, membres et réservations sont bien visibles dans l’application.

Exemple de vérification :

```sql
SELECT COUNT(*) FROM Artist;
SELECT COUNT(*) FROM Artwork;
SELECT COUNT(*) FROM Gallery;
```

Ce test a été validé.

### Test 3 - Création d’un artiste

Objectif : vérifier qu’un artiste ajouté depuis l’interface est bien enregistré dans la base.

Résultat : l’artiste ajouté apparaît directement dans le tableau de l’application et il est aussi présent dans MySQL.

Exemple de vérification :

```sql
SELECT name_artist, contactEmail_artist, city_artist
FROM Artist
WHERE contactEmail_artist = 'test.artist@artconnect.com';
```

Après fermeture puis relancement de l’application, l’artiste est toujours présent.

Ce test a été validé.

### Test 4 - Modification d’un artiste

Objectif : vérifier que la modification d’un artiste depuis l’interface est bien enregistrée.

Résultat : les nouvelles informations sont visibles dans l’application et dans la base de données.

Exemple :

```sql
SELECT city_artist
FROM Artist
WHERE name_artist = 'Lea Moreau';
```

Après redémarrage, la modification est toujours présente.

Ce test a été validé.

### Test 5 - Suppression d’un artiste

Objectif : vérifier que la suppression d’un artiste fonctionne correctement.

Résultat : l’artiste supprimé disparaît de l’interface et n’est plus présent dans la table `Artist`.

Exemple de vérification :

```sql
SELECT COUNT(*)
FROM Artist
WHERE name_artist = 'TestArtist';
```

Ce test a été validé.

### Test 6 - Création et gestion des œuvres

Objectif : vérifier que les œuvres peuvent être créées, modifiées et supprimées avec leur artiste associé.

Résultat : les œuvres ajoutées depuis l’interface sont bien enregistrées dans la table `Artwork`, avec le bon lien vers l’artiste.

Exemple de vérification :

```sql
SELECT aw.title_artwork, ar.name_artist
FROM Artwork aw
JOIN Artist ar ON aw.id_artist = ar.id_artist
WHERE aw.title_artwork = 'Nouvelle Toile';
```

Ce test a été validé.

### Test 7 - Gestion des réservations

Objectif : vérifier que les réservations d’ateliers sont bien enregistrées et reliées à un membre et à un atelier.

Résultat : les réservations créées dans l’application apparaissent dans l’onglet correspondant et sont bien présentes dans la base de données.

Exemple de vérification :

```sql
SELECT b.id_workshop, b.id_communityMember, b.paymentStatus_booking
FROM Booking b;
```

Ce test a été validé.

### Test 8 - Persistance après redémarrage

Objectif : vérifier que les données ne disparaissent pas après fermeture de l’application.

Résultat : après avoir créé ou modifié des données, nous avons fermé puis relancé l’application. Les données étaient toujours présentes.

Cela confirme que l’application utilise bien MySQL pour stocker les données, et pas seulement une mémoire temporaire.

Ce test a été validé.

### Test 9 - Recherche et filtrage

Objectif : vérifier que les recherches et filtres affichent les bons résultats.

Résultat : les recherches par nom, titre, artiste ou statut retournent des résultats cohérents avec les données présentes dans la base.

Exemple de vérification :

```sql
SELECT DISTINCT a.name_artist, d.name_discipline
FROM Artist a
LEFT JOIN Artist_Discipline ad ON a.id_artist = ad.id_artist
LEFT JOIN Discipline d ON ad.name_discipline = d.name_discipline
ORDER BY a.name_artist;
```

Ce test a été validé.

### Test 10 - Vérification des contraintes

Objectif : vérifier que certaines données incohérentes sont bloquées.

Résultat : les contraintes SQL et les contrôles de l’application empêchent les erreurs principales, comme les doublons ou certaines incohérences dans les relations.

Exemple :

```sql
INSERT INTO Artist(id_artist, name_artist, birthYear_artist, contactEmail_artist, city_artist)
VALUES (999, 'Test', 1990, 'lea.moreau@artconnect.com', 'Paris');
```

Si l’email existe déjà, la base bloque l’insertion.

Ce test a été validé.

## 9. Bilan des tests

Les tests réalisés montrent que l’intégration entre l’application JavaFX et la base de données MySQL fonctionne correctement.

Les principales fonctionnalités ont été testées directement dans l’interface :

* affichage des données ;
* création de nouveaux éléments ;
* modification de données existantes ;
* suppression d’éléments ;
* recherche et filtrage ;
* gestion des réservations ;
* vérification de la persistance après redémarrage.

Les résultats obtenus sont cohérents avec les données stockées dans MySQL. Les modifications faites dans l’application sont bien enregistrées dans la base, puis relues correctement au lancement suivant.

On peut donc conclure que la persistance JDBC est bien mise en place et que le lien entre l’application et la base de données est opérationnel.

## 10. Limites actuelles du projet

Même si l’intégration principale fonctionne, certaines parties restent volontairement simplifiées dans le cadre du projet.

Par exemple :

* certaines sections sont surtout en lecture seule ;
* il n’y a pas d’authentification utilisateur ;
* il n’y a pas encore de pagination ;
* les fichiers images ne sont pas réellement gérés ;
* les procédures stockées existent mais ne sont pas forcément toutes appelées depuis l’interface.

Ces limites ne bloquent pas le fonctionnement principal du projet. Elles correspondent plutôt à des améliorations possibles si le projet devait être poursuivi.

## 11. Conclusion

Cette étape a permis de valider le fonctionnement global du projet ArtConnect.

L’application est bien reliée à la base MySQL, les données sont persistées, les principales fonctionnalités ont été testées, et les résultats sont cohérents avec les objectifs attendus.

Les tests effectués confirment que le projet est fonctionnel sur le périmètre demandé. L’architecture mise en place reste claire, et elle permettrait d’ajouter d’autres fonctionnalités plus tard si nécessaire.