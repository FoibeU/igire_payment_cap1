package util;

import exception.DatabaseConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        String url = setting("db.url", "IGIREPAY_DB_URL");
        try {
            return DriverManager.getConnection(url, setting("db.username", "IGIREPAY_DB_USERNAME"), setting("db.password", "IGIREPAY_DB_PASSWORD"));
        } catch (SQLException e) {
            throw new DatabaseConnectionException(describe(e, url), e);
        }
    }

    private static String describe(SQLException e, String url) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        if (state.startsWith("08")) {
            return "Cannot reach PostgreSQL at " + url + ". Is the PostgreSQL LAB3.service running?";
        }
        if (state.startsWith("28")) {
            return "PostgreSQL rejected the username/password. Check db.username and db.password in db.properties.";
        }
        if ("3D000".equals(state)) {
            return "The database in " + url + " does not exist. Create it first (CREATE DATABASE customer_db;).";
        }
        if (e.getMessage() != null && e.getMessage().startsWith("No suitable driver")) {
            return "PostgreSQL JDBC driver not found. Reload Maven so the postgresql dependency is downloaded.";
        }
        return "Database connection failed: " + e.getMessage();
    }

    private static String setting(String key, String environmentVariable) {
        String fromEnvironment = System.getenv(environmentVariable);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new DatabaseConnectionException("Missing '" + key + "' in db.properties (or environment variable " + environmentVariable + ").", null);
        }
        return value;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read db.properties", e);
        }
    }
}
