package com.alphaindiamike.miiv.controllers.cli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alphaindiamike.miiv.model.filesystem.RepositoryScheme;
import com.alphaindiamike.miiv.services.ConfigurationLoaderService;
import com.alphaindiamike.miiv.services.GlobalSettingsService;
import com.alphaindiamike.miiv.services.ProgramStateService;

@Component
public class MoveCommandHandler implements CommandHandler{
	private static final Logger logger = LoggerFactory.getLogger(InitCommandHandler.class);
	// Assuming GlobalSettingsService and ProgramStateService follow the singleton
	// pattern
	private final GlobalSettingsService settingsService;
	private final ProgramStateService programState;
	private final RepositoryScheme repositoryScheme;

	@Autowired
	public MoveCommandHandler(GlobalSettingsService settingsService, ConfigurationLoaderService configurationLoader,
			ProgramStateService programState) {
		this.repositoryScheme = configurationLoader.getRepositoryScheme();
		this.settingsService = settingsService;
		this.programState = programState;
	}
    @Override
    public CommandResponse handle(String[] args) {
    	System.out.println("Not implemented :(");
		return null;
    }

    @Override
    public CommandResponse supports(String command) {
		if (command.equals("move") && (settingsService.getSetting("workspace_dir") == null)) {
			String message = "Workspace not initialized. Use miiv init {new_workspace} to define the workspace location.";
			System.out.println(message);
			return new CommandResponse("", message, false);
		}
		if (command.equals("move") && (settingsService.getSetting("workspace_dir") != null)) {
	        return new CommandResponse("","",true);
		}
        return new CommandResponse("",
        		"",
        		false);
    }
}
