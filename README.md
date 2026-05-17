# ArtConnect

<p align="center">
  <strong>A JavaFX + MySQL application for managing a local art community</strong><br>
  Built as a student project with a clean JDBC-based architecture and persistent data management.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-orange" alt="Java 17+">
  <img src="https://img.shields.io/badge/JavaFX-UI-blue" alt="JavaFX">
  <img src="https://img.shields.io/badge/MySQL-Database-4479A1" alt="MySQL">
  <img src="https://img.shields.io/badge/Maven-Build-C71A36" alt="Maven">
  <img src="https://img.shields.io/badge/JDBC-Integrated-success" alt="JDBC Integrated">
  <img src="https://img.shields.io/badge/Step%204-Complete-brightgreen" alt="Step 5 Complete">
</p>

---

## Overview

**ArtConnect** is a student project built with **JavaFX** and **MySQL** to manage data related to a local art community.

The project combines:
- a **JavaFX desktop application**
- a **MySQL relational database**
- a **JDBC-based service layer** for runtime persistence

The current version includes full integration of **Step 4**, with the application connected to MySQL at runtime.

---

## Repository Structure

```text
ArtConnect/
├── ArtConnectBDD/       # SQL schema and data scripts
└── ArtConnectPro-App/   # JavaFX application with JDBC services
```

| Folder              | Description                                                      |
|---------------------|------------------------------------------------------------------|
| `ArtConnectBDD`     | Contains SQL scripts for schema creation and data initialization |
| `ArtConnectPro-App` | Contains the JavaFX application and JDBC-based business logic    |

---

## Step 4 Status

> **Step 4 is fully integrated and operational.**

The application is now wired to a MySQL database through JDBC services at runtime, using a `ServiceProvider`-based architecture.

---

## Implemented Features

### CRUD Features

#### Artists tab

- Create artists
- Update artists
- Delete artists
- Persist changes through JDBC

#### Artworks tab

- Create artworks
- Update artworks
- Delete artworks
- Persist changes through JDBC

### Read-Only Data Views

The following tabs are connected to JDBC-backed services and currently provide **read-only** access:

- Discover
- Galleries
- Exhibitions
- Workshops
- Community

### Runtime Architecture

- JDBC services are injected and used through **`ServiceProvider`**
- Data is loaded dynamically from MySQL at runtime
- Artist and artwork modifications persist between sessions

---

## Requirements

Before running the project, make sure you have:

- **Java 17+**
- **Maven 3.8+**
- **MySQL** running locally or accessible from your machine

---

## Database Configuration

If no environment variables are provided, the application uses the following default values:

| Setting  | Default Value |
|----------|---------------|
| Database | `ArtConnect`  |
| User     | `root`        |
| Password | `password`    |

---

## SQL Setup

Run the scripts from the `ArtConnectBDD` folder in the following order:

1. `tablesPrincipales.sql`
2. `insertionDonnees.sql`

### Optional Scripts

These are **not required** for Step 4 runtime execution:

- `transactions.sql`
- `triggers.sql`
- `vuesIndex.sql`

---

## Environment Variables

If your local MySQL setup is different, define these variables:

- `ARTCONNECT_DB_URL`
- `ARTCONNECT_DB_USER`
- `ARTCONNECT_DB_PASSWORD`

### Example: Linux / macOS

```bash
export ARTCONNECT_DB_URL="jdbc:mysql://localhost:3306/ArtConnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export ARTCONNECT_DB_USER="your_user"
export ARTCONNECT_DB_PASSWORD="your_password"
```

### Example: Windows PowerShell

```powershell
setx ARTCONNECT_DB_URL "jdbc:mysql://localhost:3306/ArtConnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
setx ARTCONNECT_DB_USER "your_user"
setx ARTCONNECT_DB_PASSWORD "your_password"
```

---

## Running the Application

From the `ArtConnectPro-App` directory:

```bash
mvn clean javafx:run
```

---

## Quick Testing Checklist

Use this checklist to validate the project setup and Step 4 behavior:

- [ ] The application launches without JDBC connection errors
- [ ] Artists can be added, updated, and deleted
- [ ] Artist changes are still present after restarting the app
- [ ] Artworks can be added, updated, and deleted
- [ ] Artwork changes are still present after restarting the app
- [ ] Artist search/filter works correctly (`name`, `city`, `email`, and `discipline`)
- [ ] Artwork search works correctly (`title`, `artist`, and `type`)
- [ ] Read-only tabs load data successfully from JDBC services

---

## Known Limitations

The current scope intentionally keeps some parts simple:

- The **Artist form** does not allow editing disciplines directly (`discipline` is currently used for filtering only)
- The **Artwork** table remains intentionally minimal (no `medium`, `dimensions`, `description`, or `tags` columns)
- Tabs other than **Artists** and **Artworks** are **read-only** in Step 4
- Artist update/delete operations assume **unique artist names**
- Artwork update/delete operations assume **unique artwork titles**

---

## Tech Stack

- **Java 17**
- **JavaFX**
- **MySQL**
- **JDBC**
- **Maven**

---

## Project Goal

This project was developed as part of a student workflow to demonstrate:

- JavaFX desktop UI development
- relational database modeling with MySQL
- JDBC integration in a real application
- service-oriented runtime wiring
- persistent CRUD operations in a structured project

---

## Summary

**ArtConnect** is a functional student desktop application that connects a JavaFX interface to a MySQL database using JDBC.  
At its current stage, it supports persistent CRUD for core entities and JDBC-backed visualization for the rest of the platform.

It is a solid foundation for expanding into a richer community art management system.
