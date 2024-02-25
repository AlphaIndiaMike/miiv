package com.alphaindiamike.miiv.services;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Manages global settings in a dedicated SQLite database.
 */
@Service
public class GlobalSettingsService {
    private static final Logger logger = LoggerFactory.getLogger(GlobalSettingsService.class);
    private static GlobalSettingsService instance;
    private Connection connection;
    private final String dbPath = ".miiv_global/globalSettings.db";

    private GlobalSettingsService() {
        initializeDatabase();
    }

    /**
     * Retrieves the singleton instance of GlobalSettingsService.
     * @return The singleton instance.
     */
    public static synchronized GlobalSettingsService getInstance() {
        if (instance == null) {
            instance = new GlobalSettingsService();
        }
        return instance;
    }

    /**
     * Initializes the database connection and schema.
     */
    private void initializeDatabase() {
        try {
            ensureDirectoryExists(dbPath);
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            initializeSchema();
        } catch (SQLException e) {
            logger.error("Failed to initialize the database: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize the database", e);
        }
    }

    private void ensureDirectoryExists(String path) throws SQLException {
        File dbFile = new File(path);
        File dbDirectory = dbFile.getParentFile();
        if (!dbDirectory.exists() && !dbDirectory.mkdirs()) {
            throw new SQLException("Failed to create directory: " + dbDirectory.getPath());
        }
    }

    private void initializeSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS global_settings (key TEXT PRIMARY KEY, value TEXT)");
        }
    }

    /**
     * Sets a global setting.
     * @param key The setting key.
     * @param value The setting value.
     */
    public void setSetting(String key, String value) {
        String sql = "REPLACE INTO global_settings (key, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to set setting for key {}: {}", key, e.getMessage());
        }
    }

    /**
     * Retrieves a global setting.
     * @param key The setting key.
     * @return The setting value or null if not found.
     */
    public String getSetting(String key) {
        String sql = "SELECT value FROM global_settings WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get setting for key {}: {}", key, e.getMessage());
        }
        return null;
    }

    /**
     * Deletes a global setting.
     * @param key The setting key to be deleted.
     */
    public void deleteSetting(String key) {
        String sql = "DELETE FROM global_settings WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                logger.info("No setting found to delete for key: {}", key);
            } else {
                logger.info("Successfully deleted setting for key: {}", key);
            }
        } catch (SQLException e) {
            logger.error("Error deleting setting for key {}: {}", key, e.getMessage());
        }
    }
}
