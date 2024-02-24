package com.alphaindiamike.miiv.units;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alphaindiamike.miiv.services.GlobalSettingsService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class GlobalSettingsServiceUT {

    private String testDbPath = ".miiv_global/globalSettings.db";
    private GlobalSettingsService service;

    @BeforeEach
    void setUp() throws Exception {
    	resetGlobalSettingsServiceInstance();
        // Adjust the getInstance method to accept dbPath for testing purpose
        service = GlobalSettingsService.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Attempt to delete the test database with retries
        final int maxRetries = 5; // Maximum number of retries
        final int retryDelay = 1000; // Delay between retries in milliseconds (1 second)
        boolean deleted = false;
        int retryCount = 0;

        while (!deleted && retryCount < maxRetries) {
            try {
                Files.deleteIfExists(Paths.get(testDbPath));
                new File(".miiv_global").delete(); // Attempt to delete the directory
                deleted = true; // If deletion is successful, exit the loop
            } catch (Exception e) {
                try {
                    Thread.sleep(retryDelay); // Wait for a bit before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    throw new RuntimeException("Interrupted while waiting to retry file deletion", ie);
                }
                retryCount++;
            }
        }

        if (!deleted) {
            System.err.println("Failed to delete the test database or directory after " + maxRetries + " retries.");
        }
        
        resetGlobalSettingsServiceInstance();
    }
    
    private void resetGlobalSettingsServiceInstance() throws Exception {
        Field instance = GlobalSettingsService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null); // Set the static instance to null
    }


    @Test
    void testSetAndGetSetting() {
        String key = "testKey";
        String value = "testValue";
        service.setSetting(key, value);

        String retrievedValue = service.getSetting(key);
        assertEquals(value, retrievedValue, "The retrieved value should match the set value.");
    }

    @Test
    void testGetSettingNonExistent() {
        String nonExistentKey = "nonExistentKey";
        assertNull(service.getSetting(nonExistentKey), "Retrieving a non-existent key should return null.");
    }
    
    @Test
    void testUpdateExistingSetting() {
        String key = "updateKey";
        String initialValue = "initialValue";
        String updatedValue = "updatedValue";
        
        // Set initial value
        service.setSetting(key, initialValue);
        // Update to new value
        service.setSetting(key, updatedValue);
        
        String retrievedValue = service.getSetting(key);
        assertEquals(updatedValue, retrievedValue, "The retrieved value should match the updated value.");
    }

    @Test
    void testSetAndGetMultipleSettings() {
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        
        // Set multiple settings
        service.setSetting(key1, value1);
        service.setSetting(key2, value2);
        
        // Retrieve and verify both settings
        assertEquals(value1, service.getSetting(key1), "The retrieved value for key1 should match the set value.");
        assertEquals(value2, service.getSetting(key2), "The retrieved value for key2 should match the set value.");
    }

    @Test
    void testDeleteSetting() {
        String key = "deleteKey";
        String value = "value";
        
        // Set and then delete the setting
        service.setSetting(key, value);
        service.deleteSetting(key); // Assumes implementation of a method to delete a setting
        
        assertNull(service.getSetting(key), "Retrieving a deleted key should return null.");
    }

}
