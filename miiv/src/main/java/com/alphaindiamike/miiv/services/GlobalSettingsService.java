package com.alphaindiamike.miiv.services;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalSettingsService {
	private static final Logger logger = LoggerFactory.getLogger(GlobalSettingsService.class);
    private static GlobalSettingsService instance;
    private Connection connection;
    private final String dbPath;

    private GlobalSettingsService() {
        this.dbPath = ".miiv_global/globalSettings.db";
        initializeDatabase();
    }

    public static synchronized GlobalSettingsService getInstance() {
        if (instance == null) {
            instance = new GlobalSettingsService();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            File dbFile = new File(dbPath);
            File dbDirectory = dbFile.getParentFile();
            if (!dbDirectory.exists() && !dbDirectory.mkdirs()) {
                throw new IllegalStateException("Failed to create directory: " + dbDirectory.getPath());
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS global_settings (key TEXT PRIMARY KEY, value TEXT)");
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            throw new RuntimeException("Failed to initialize the database", e);
        }
    }

    public void setSetting(String key, String value) {
        String sql = "REPLACE INTO global_settings (key, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to set setting: " + e.getMessage());
        }
    }

    public String getSetting(String key) {
        String sql = "SELECT value FROM global_settings WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Failed to get setting: " + e.getMessage());
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