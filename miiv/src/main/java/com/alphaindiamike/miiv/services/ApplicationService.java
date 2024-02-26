package com.alphaindiamike.miiv.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alphaindiamike.miiv.controllers.MiivControllerAccessInterface;
import com.alphaindiamike.miiv.model.filesystem.RepositoryScheme;

@Service
public class ApplicationService {
    private final MiivControllerAccessInterface controller;
    private final RepositoryScheme repositoryScheme;

    @Autowired
    public ApplicationService(@Qualifier("commandLineController") MiivControllerAccessInterface controller,
    		ConfigurationLoaderService configurationLoader) {
        this.controller = controller;
        this.repositoryScheme = configurationLoader.getRepositoryScheme();
    }

	public void performAction(String[] args) {
		this.controller.input(args);
	}
	
	public RepositoryScheme getRepositoryScheme() {
		return repositoryScheme;
	}
	
	public String getErrorResponse() {
		return this.controller.error();
	}
	
	public String getResponse() {
		return this.controller.output();
	}
	
	public boolean getIsPositiveResponse() {
		return this.controller.finished();
	}
    
}