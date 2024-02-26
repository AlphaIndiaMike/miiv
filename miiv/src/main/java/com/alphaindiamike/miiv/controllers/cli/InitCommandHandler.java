package com.alphaindiamike.miiv.controllers.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alphaindiamike.miiv.model.filesystem.RepositoryScheme;
import com.alphaindiamike.miiv.privatelib.functions.FileSystemFunctionsLib;
import com.alphaindiamike.miiv.services.ConfigurationLoaderService;
import com.alphaindiamike.miiv.services.GlobalSettingsService;
import com.alphaindiamike.miiv.services.ProgramStateService;
import com.alphaindiamike.miiv.services.workflow.WorkspaceReadyState;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;

@Component
public class InitCommandHandler implements CommandHandler{
    private static final Logger logger = LoggerFactory.getLogger(InitCommandHandler.class);

    // Assuming GlobalSettingsService and ProgramStateService follow the singleton pattern
    private final GlobalSettingsService settingsService;
    private final ProgramStateService programState;
    private final RepositoryScheme repositoryScheme;
    
    @Autowired
    public InitCommandHandler(GlobalSettingsService settingsService,
    		ConfigurationLoaderService configurationLoader,
    		ProgramStateService programState) {
		this.repositoryScheme = configurationLoader.getRepositoryScheme();
    	this.settingsService = settingsService;
    	this.programState = programState;
    }

    @Override
    public CommandResponse handle(String[] args) {
        // Check if 'workspace_dir' setting exists, indicating 'init' has already been executed
        if (settingsService.getSetting("workspace_dir") != null) {
        	String message = "Workspace already initialized. Use miiv set {new_workspace} to change the workspace location.";
            System.out.println(message);
            return new CommandResponse("",message,false);
        }
    	
        // Command structure validation
        if (args.length == 0 || !args[0].equals("init") || args.length > 2) {
        	String message = "Incorrect command. Usage: miiv init {valid path}";
            logger.error(message);
            return new CommandResponse("",message,false);
        }

        // Path validation for the "init" command
        if (args.length == 2) {
            try {
                Path path = Paths.get(args[1]);
                if (!Files.exists(path)) {
                	String message ="The provided path does not exist. Please provide a valid directory path.";
                    logger.error(message);
                    return new CommandResponse("",message,false);
                } else if (!Files.isDirectory(path)) {
                	String message ="The provided path is not a directory. Please provide a valid directory path.";
                    logger.error(message);
                    return new CommandResponse("",message,false);
                }

                // Valid path: Proceed with initialization logic
                settingsService.setSetting("workspace_dir", path.toString());
                FileSystemFunctionsLib.createDirectoryStructure(repositoryScheme, path.toString());
                programState.triggerStateTransition(new WorkspaceReadyState(path.toString()), new String[]{path.toString()});
                String message = "Initialized workspace succesfully at " + settingsService.getSetting("workspace_dir");
                System.out.println(message);
                return new CommandResponse(message,"",true);

            } catch (InvalidPathException e) {
                logger.error("The provided path is invalid: {}", e.getMessage());
            }
        } else {
            // Missing path
            logger.error("Missing path. Usage: miiv init {valid path}");
        }
        return new CommandResponse("", "Missing or invalid path!", false);
    }

    @Override
    public boolean supports(String command) {
        return "init".equals(command.toLowerCase());
    }
}