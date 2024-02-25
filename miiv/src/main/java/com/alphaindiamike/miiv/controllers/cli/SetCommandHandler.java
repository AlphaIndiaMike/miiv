package com.alphaindiamike.miiv.controllers.cli;
import org.springframework.stereotype.Component;

@Component
public class SetCommandHandler implements CommandHandler{
    @Override
    public CommandResponse handle(String[] args) {
    	System.out.println("Not implemented :(");
		return null;
    }

    @Override
    public boolean supports(String command) {
        return "set".equals(command.toLowerCase());
    }
}
