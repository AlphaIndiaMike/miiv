package com.alphaindiamike.miiv.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alphaindiamike.miiv.controllers.MiivControllerAccessInterface;

@Service
public class ApplicationService {
    private final MiivControllerAccessInterface controller;

    @Autowired
    public ApplicationService(@Qualifier("commandLineController") MiivControllerAccessInterface controller) {
        this.controller = controller;
    }

	public void performAction(String[] args) {
		this.controller.input(args);
		
	}

    // Now, you can use the controller in your service methods
    
}