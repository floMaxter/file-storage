package com.projects.filestorage.exception;

public class UnauthenticatedAccessException extends GenericApplicationException {
    public UnauthenticatedAccessException(String message) {
        super(message);
    }
}
