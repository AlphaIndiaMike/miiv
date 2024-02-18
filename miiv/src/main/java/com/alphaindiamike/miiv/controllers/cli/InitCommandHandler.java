package com.alphaindiamike.miiv.controllers.cli;

import org.springframework.stereotype.Component;

@Component
public class InitCommandHandler implements CommandHandler{
    @Override
    public void handle(String[] args) {
    	System.out.println("Not implemented :(");
    }

    @Override
    public boolean supports(String command) {
        return "init".equals(command.toLowerCase());
    }
}
