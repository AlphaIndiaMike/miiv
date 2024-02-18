package com.alphaindiamike.miiv.controllers;

public interface MiivControllerAccessInterface {
    void input(String[] args);
    String output();
    String error();
    boolean finished();
    void subscribe(MiivControllerAccessSubscriberInterface subscriber);
}
