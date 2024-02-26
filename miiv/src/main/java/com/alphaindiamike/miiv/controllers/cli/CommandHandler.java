package com.alphaindiamike.miiv.controllers.cli;

public interface CommandHandler {
    CommandResponse handle(String[] args);
    boolean supports(String command);
}
