package com.alphaindiamike.miiv.privatelib.exceptions;

public class InvalidPathException extends RuntimeException {
    public InvalidPathException(String message) {
        super(message);
    }

    public InvalidPathException(String message, Throwable cause) {
        super(message, cause);
    }
}