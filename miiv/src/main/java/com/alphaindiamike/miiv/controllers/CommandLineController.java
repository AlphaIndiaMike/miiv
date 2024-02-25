package com.alphaindiamike.miiv.controllers;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.alphaindiamike.miiv.controllers.cli.CommandHandler;
import com.alphaindiamike.miiv.controllers.cli.CommandResponse;
import com.alphaindiamike.miiv.controllers.cli.HelpCommandHandler;


@Component
@Qualifier("commandLineController")
public class CommandLineController implements MiivControllerAccessInterface{
	
	private final List<CommandHandler> commandHandlers;
	private CommandResponse response;
	
    @Autowired
    public CommandLineController(List<CommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }
    
    @Autowired
    private HelpCommandHandler helpCommandHandler;

	@Override
	public void input(String[] command) {
		if (command.length == 0) {
            System.out.println("No command provided.\n\n");
            helpCommandHandler.handle(new String[0]);
            return;
        }
        
		String prefix = command[0];
        for (CommandHandler handler : commandHandlers) {
            if (handler.supports(prefix)) {
            	response = handler.handle(Arrays.copyOfRange(command, 0, command.length));
                return;
            }
        }

        System.out.println("Invalid command.\n\n");
        helpCommandHandler.handle(new String[0]);
	}

	@Override
	public String output() {
		return response != null ? response.Response() : "No response available.";
	}

	@Override
	public String error() {
		return response != null ? response.Error() : "No error.";
	}

	@Override
	public boolean finished() {
		return response != null && response.PR();
	}

	@Override
	public void subscribe(MiivControllerAccessSubscriberInterface subscriber) {
		// TODO Auto-generated method stub
		
	}

}
