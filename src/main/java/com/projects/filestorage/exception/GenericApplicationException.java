package com.projects.filestorage.exception;

public class GenericApplicationException extends RuntimeException {
    public GenericApplicationException(String message) {
        super(message);
    }

    public GenericApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
