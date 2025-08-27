package com.projects.filestorage.exception;

public class ResourceNotFoundException extends GenericApplicationException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
