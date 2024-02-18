package com.alphaindiamike.miiv.controllers.cli;

public interface CommandHandler {
    void handle(String[] args);
    boolean supports(String command);
}
