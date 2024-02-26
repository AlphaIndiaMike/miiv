package com.alphaindiamike.miiv.controllers.cli;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

@Component
public class SetCommandHandler implements CommandHandler{
	private static final Logger logger = LoggerFactory.getLogger(InitCommandHandler.class);
    // Assuming GlobalSettingsService and ProgramStateService follow the singleton pattern
    private final GlobalSettingsService settingsService;
    private final ProgramStateService programState;
    private final RepositoryScheme repositoryScheme;
    
    @Autowired
    public SetCommandHandler(GlobalSettingsService settingsService,
    		ConfigurationLoaderService configurationLoader,
    		ProgramStateService programState) {
		this.repositoryScheme = configurationLoader.getRepositoryScheme();
    	this.settingsService = settingsService;
    	this.programState = programState;
    }
    
    @Override
    public CommandResponse handle(String[] args) {
    	String defaultMessage = "Miiv Command Reference:\n\n" +
    		    "1. set: Changes the current workspace directory.\n" +
    		    "   Usage: miiv set {valid path}\n\n" +
    		    "2. copy: Copies files from the specified source to the target locations within the workspace.\n" +
    		    "   Usage: miiv copy <source> <arguments>\n\n" +
    		    "3. move: Moves files from the specified source to the target locations within the workspace.\n" +
    		    "   Usage: miiv move <source> <arguments>\n\n" +
    		    "Incorrect command. Usage: miiv set {valid path}, miiv copy <source> <arguments>, miiv move <source> <arguments>\n";
    	// Check for no command or unrecognized command
    	if (args.length == 0 || args.length > 2) {
    	    System.out.println(defaultMessage);
    	    return new CommandResponse("", defaultMessage, false);
    	}
    	if (args[0].equals("init")) {
    	    String message = "Workspace already initialized. Use miiv set {new_workspace} to change the workspace location.";
    	    System.out.println(message);
    	    return new CommandResponse("", message, false);
    	}
    	if (!args[0].equals("set")) {
    		System.out.println(defaultMessage);
    	    return new CommandResponse("", defaultMessage, false);
    	}
    	if (settingsService.getSetting("workspace_dir") == null) {
    		String message = "Workspace not initialized. Use miiv init {workspace_dir} to initialize the workspace location.";
    	    System.out.println(message);
    	    return new CommandResponse("", message, false);
    	}
    	
        
     // Path validation for the "set" command
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
                String message = "Workspace succesfully changed to: " + settingsService.getSetting("workspace_dir");
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
        return "set".equals(command.toLowerCase());
    }
}
