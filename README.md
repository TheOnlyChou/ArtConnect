# ArtConnect

## Portée
Ce README documente l’ensemble du dépôt ainsi que l’état actuel de l’intégration de l’étape 4.

Dossiers du dépôt :
- ArtConnectBDD : schéma SQL et scripts de données
- ArtConnectPro-App : application JavaFX avec intégration JDBC

## État actuel de l’étape 4
L’application Java est désormais reliée à des services basés sur JDBC via `ServiceProvider`.

Implémenté dans le code :
- gestion de la connexion JDBC via `DatabaseConfig` et `ConnectionManager`
- classes DAO JDBC pour `Artist`, `Artwork`, `CommunityMember`, `Exhibition`, `Gallery`, `Workshop`
- implémentations de services basées sur JDBC utilisées par les contrôleurs UI
- dépendance du driver MySQL déclarée dans Maven

Références de fichiers importantes :
- [ArtConnectPro-App/src/main/java/com/project/artconnect/config/DatabaseConfig.java](ArtConnectPro-App/src/main/java/com/project/artconnect/config/DatabaseConfig.java)
- [ArtConnectPro-App/src/main/java/com/project/artconnect/util/ConnectionManager.java](ArtConnectPro-App/src/main/java/com/project/artconnect/util/ConnectionManager.java)
- [ArtConnectPro-App/src/main/java/com/project/artconnect/util/ServiceProvider.java](ArtConnectPro-App/src/main/java/com/project/artconnect/util/ServiceProvider.java)
- [ArtConnectPro-App/pom.xml](ArtConnectPro-App/pom.xml)

## Vérification de DatabaseConfig
Le `DatabaseConfig` actuel est suffisant pour une exécution en local tel quel, à condition que votre configuration MySQL locale corresponde à l’une de ces options :

- Mode par défaut :
  - l’URL utilise la base locale `ArtConnect` sur le port `3306`
  - l’utilisateur est `root`
  - le mot de passe est `password`
- Mode surcharge par variables d’environnement :
  - définir `ARTCONNECT_DB_URL`, `ARTCONNECT_DB_USER`, `ARTCONNECT_DB_PASSWORD`

Comportement important à l’exécution :
- `DatabaseConfig` utilise des valeurs par défaut de secours.
- L’application ne fonctionnera que si ces valeurs par défaut correspondent à votre vraie configuration MySQL locale.
- Si ce n’est pas le cas, les appels de démarrage ou d’exécution entre l’interface et les services/DAO échoueront avec des erreurs JDBC, comme par exemple un accès refusé.

Si vos identifiants MySQL sont différents de `root/password`, vous devez les surcharger avec des variables d’environnement.

## Ce qui est déjà pris en charge dans le code
- la dépendance du driver JDBC est incluse dans les dépendances Maven
- l’URL JDBC, l’utilisateur et le mot de passe sont centralisés dans `DatabaseConfig`
- des valeurs par défaut de secours existent lorsque les variables d’environnement ne sont pas définies
- `ServiceProvider` utilise déjà les services JDBC

## Ce que vous devez configurer manuellement
Vous devez encore effectuer tous les éléments ci-dessous avant le premier lancement.

1. Vérifier que Java et Maven sont installés
- Java 17+
- Maven 3.8+

2. Vérifier que le serveur MySQL est démarré
- confirmer que l’hôte et le port sont accessibles
- confirmer que l’utilisateur MySQL peut créer le schéma et modifier les tables

3. Créer et alimenter la base de données
- exécuter les scripts de `ArtConnectBDD` dans cet ordre :
  1. `tablesPrincipales.sql`
  2. `insertionDonnees.sql`
- scripts optionnels à exécuter plus tard pour les exercices SQL avancés :
  - `transactions.sql`
  - `triggers.sql`
  - `vuesIndex.sql`

4. Configurer les identifiants si les valeurs par défaut ne correspondent pas à votre machine
- Exemple Linux ou macOS :
  - `export ARTCONNECT_DB_URL="jdbc:mysql://localhost:3306/ArtConnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"`
  - `export ARTCONNECT_DB_USER="your_user"`
  - `export ARTCONNECT_DB_PASSWORD="your_password"`
- Exemple Windows PowerShell :
  - `setx ARTCONNECT_DB_URL "jdbc:mysql://localhost:3306/ArtConnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"`
  - `setx ARTCONNECT_DB_USER "your_user"`
  - `setx ARTCONNECT_DB_PASSWORD "your_password"`

Variables d’environnement explicitement utilisées par le code :
- `ARTCONNECT_DB_URL`
- `ARTCONNECT_DB_USER`
- `ARTCONNECT_DB_PASSWORD`

5. Contexte d’exécution dans l’IDE ou le terminal
- exécuter l’application depuis `ArtConnectPro-App` afin que Maven utilise le bon `pom.xml`
- si vous lancez depuis un IDE, vérifier que les variables d’environnement sont bien définies dans la configuration d’exécution si nécessaire

## Checklist de connectivité à la base de données (avant exécution)
- le serveur MySQL est démarré et accessible sur l’hôte/port configuré ;
- l’utilisateur et le mot de passe configurés sont valides ;
- l’utilisateur configuré a les permissions nécessaires pour se connecter à `ArtConnect` et exécuter des requêtes ;
- la base de données `ArtConnect` existe ;
- les tables requises existent (par exemple `Artist`, `Artwork`, `Gallery`, `Exhibition`, `Workshop`, `CommunityMember`) ;
- le script du schéma et le script d’insertion ont été exécutés dans le bon ordre.

## Comment lancer
Depuis `ArtConnectPro-App` :

- Compiler :
  - `mvn -DskipTests compile`
- Lancer l’application JavaFX :
  - `mvn clean javafx:run`

## Exemple d’exécution des scripts SQL
Depuis une session client MySQL :

- `SOURCE /absolute/path/to/ArtConnectBDD/tablesPrincipales.sql;`
- `SOURCE /absolute/path/to/ArtConnectBDD/insertionDonnees.sql;`

## Dépannage
- Accès refusé pour l’utilisateur :
  - l’utilisateur/mot de passe configuré est invalide pour MySQL
  - mettre à jour `ARTCONNECT_DB_USER` et `ARTCONNECT_DB_PASSWORD`
  - vérifier les privilèges sur la base cible
- Base de données introuvable (`ArtConnect`) :
  - `ArtConnectBDD/tablesPrincipales.sql` n’a pas été exécuté
  - l’URL pointe vers le mauvais nom de base
- Tables manquantes :
  - le script du schéma ne s’est pas terminé correctement
  - les scripts ont été exécutés dans le mauvais ordre
  - réexécuter `tablesPrincipales.sql`, puis `insertionDonnees.sql`
- Échec de communication :
  - MySQL n’est pas démarré, mauvais hôte, mauvais port, ou problème de pare-feu
- L’application démarre mais aucune donnée n’apparaît :
  - `insertionDonnees.sql` n’a pas été exécuté