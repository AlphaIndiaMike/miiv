package com.alphaindiamike.miiv.controllers.cli;
import org.springframework.stereotype.Component;

@Component
public class MoveCommandHandler implements CommandHandler{
    @Override
    public CommandResponse handle(String[] args) {
    	System.out.println("Not implemented :(");
		return null;
    }

    @Override
    public boolean supports(String command) {
        return "move".equals(command.toLowerCase());
    }
}
