package com.alphaindiamike.miiv.services;

import java.io.IOException;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alphaindiamike.miiv.model.filesystem.RepositoryScheme;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class ConfigurationLoaderService {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoaderService.class);
	private RepositoryScheme repositoryScheme;

    public ConfigurationLoaderService() {
        // Adjust the path as necessary
        String configFilePath = "structure.json";
        ObjectMapper mapper = new ObjectMapper();
        try {
            repositoryScheme = mapper.readValue(new File(configFilePath), RepositoryScheme.class);
            logger.debug("Configuration loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Failed to load configuration.", e);
        }
    }

    public RepositoryScheme getRepositoryScheme() {
        return repositoryScheme;
    }
}
