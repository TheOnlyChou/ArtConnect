package com.project.artconnect.config;

/**
 * Database configuration constants.
 */
public class DatabaseConfig {
    public static final String URL = System.getenv().getOrDefault(
            "ARTCONNECT_DB_URL",
            "jdbc:mysql://localhost:3306/ArtConnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    public static final String USER = System.getenv().getOrDefault("ARTCONNECT_DB_USER", "root");
    public static final String PASSWORD = System.getenv().getOrDefault("ARTCONNECT_DB_PASSWORD", "password");

    private DatabaseConfig() {
    }
}
