# ArtConnect

## Project Overview
ArtConnect is a student JavaFX + MySQL project for managing local art community data.

Repository structure:
- ArtConnectBDD: SQL schema and data scripts
- ArtConnectPro-App: JavaFX application with JDBC services

## Current Step 4 Status
Step 4 is integrated and running with JDBC/MySQL at runtime.

## Implemented Features
- Artists tab: CRUD with JDBC persistence
- Artworks tab: CRUD with JDBC persistence
- Discover, Galleries, Exhibitions, Workshops, Community tabs: JDBC-backed read-only views
- Runtime wiring uses JDBC services through ServiceProvider

## Database Setup
Requirements:
- MySQL running locally or reachable from your machine
- Java 17+
- Maven 3.8+

Default database values used by the app (if no environment override):
- Database: ArtConnect
- User: root
- Password: password

## SQL Execution Order
Run scripts from ArtConnectBDD in this order:
1. tablesPrincipales.sql
2. insertionDonnees.sql

Optional (not required for Step 4 app runtime):
- transactions.sql
- triggers.sql
- vuesIndex.sql

## Environment Variables
Set these if your local MySQL config is different:
- ARTCONNECT_DB_URL
- ARTCONNECT_DB_USER
- ARTCONNECT_DB_PASSWORD

Example Linux or macOS:
- export ARTCONNECT_DB_URL="jdbc:mysql://localhost:3306/ArtConnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
- export ARTCONNECT_DB_USER="your_user"
- export ARTCONNECT_DB_PASSWORD="your_password"

Example Windows PowerShell:
- setx ARTCONNECT_DB_URL "jdbc:mysql://localhost:3306/ArtConnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
- setx ARTCONNECT_DB_USER "your_user"
- setx ARTCONNECT_DB_PASSWORD "your_password"

## Run the Java Application
From ArtConnectPro-App:
1. mvn clean javafx:run

## Short Testing Checklist
- App launches without JDBC connection errors
- Artists: add, update, delete, then restart app and verify persistence
- Artworks: add, update, delete, then restart app and verify persistence
- Artists search/filter works (name/city/email + discipline filter)
- Artworks search works (title/artist/type)
- Read-only tabs load data from JDBC services

## Known Limitations and Scope Decisions
- Artist form does not edit disciplines (filter only)
- Artwork main table stays simple (no medium/dimensions/description/tags columns)
- Non-Artists and non-Artworks tabs are read-only in Step 4
- Artist update/delete actions assume unique artist names.
- Artwork update/delete actions assume unique artwork titles.