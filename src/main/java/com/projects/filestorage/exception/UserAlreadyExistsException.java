package com.projects.filestorage.exception;


public class UserAlreadyExistsException extends GenericApplicationException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
