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
    public void handle(String[] args) {
        // Display the general help text or specific command help
        if (args.length == 0) {
        	System.out.println(generalHelp);
        }
    }

    @Override
    public boolean supports(String command) {
        return "help".equals(command.toLowerCase()) || "--help".equals(command.toLowerCase());
    }
}
