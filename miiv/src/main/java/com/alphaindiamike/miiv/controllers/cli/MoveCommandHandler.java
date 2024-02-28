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
    public CommandResponse supports(String command) {
        return new CommandResponse("",
        		"Not implemented :(",
        		false);
    }
}
