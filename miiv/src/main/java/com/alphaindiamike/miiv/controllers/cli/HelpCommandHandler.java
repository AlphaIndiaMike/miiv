package com.alphaindiamike.miiv.controllers.cli;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler implements CommandHandler{
	
	private String generalHelp = "";
	
	public HelpCommandHandler() {
		ResourceBundle helpTextBundle = ResourceBundle.getBundle("help-text");
		generalHelp = helpTextBundle.getString("help.general");
	}
	
    @Override
    public CommandResponse handle(String[] args) {
        // Display the general help text or specific command help
        if (args.length == 0) {
        	System.out.println(generalHelp);
        }
		return new CommandResponse(generalHelp,"",true);
    }

    @Override
    public CommandResponse supports(String command) {
    	if ("help".equals(command.toLowerCase()) || "--help".equals(command.toLowerCase())) {
	        return new CommandResponse(generalHelp,
	        		"",
	        		true);
    	}
        return new CommandResponse("","",false);
    }
}
