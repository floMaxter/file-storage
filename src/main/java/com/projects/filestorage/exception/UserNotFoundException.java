package com.projects.filestorage.exception;

public class UserNotFoundException extends GenericApplicationException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
