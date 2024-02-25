package com.alphaindiamike.miiv.privatelib.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alphaindiamike.miiv.model.filesystem.RepositoryScheme;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

public class FileSystemFunctionsLib {
	private static final Logger logger = LoggerFactory.getLogger(FileSystemFunctionsLib.class);

    /**
     * Creates the directory structure based on the RepositoryScheme and the specified root path.
     * Enhancements include advanced logging and path validation.
     * 
     * @param repositoryScheme The repository scheme representing the directory structure.
     * @param rootPath The root path where the directory structure should be created.
     */
    public static void createDirectoryStructure(RepositoryScheme repositoryScheme, String rootPath) {
        // Validate the root path before proceeding.
        Path root = Paths.get(rootPath);
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            logger.error("The provided root path is invalid or does not exist: {}", rootPath);
            return;
        }

        // The root directory is treated as the target directory.
        if (repositoryScheme.getChildren() != null) {
            repositoryScheme.getChildren().forEach(child -> createDirectoryRecursive(child, root));
        }
    }

    private static void createDirectoryRecursive(RepositoryScheme directory, Path parentPath) {
        if (directory == null) return;

        Path currentPath = parentPath.resolve(directory.getName());
        try {
            if (!Files.exists(currentPath)) {
                Files.createDirectories(currentPath);
                logger.debug("Created directory: {}", currentPath);
            }

            // Recursively create subdirectories
            List<RepositoryScheme> subdirectories = directory.getChildren();
            if (subdirectories != null) {
                for (RepositoryScheme subdirectory : subdirectories) {
                    createDirectoryRecursive(subdirectory, currentPath);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to create directory: {} due to {}", currentPath, e.getMessage());
        }
    }
}
