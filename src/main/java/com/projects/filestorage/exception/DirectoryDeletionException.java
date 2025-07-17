package com.projects.filestorage.exception;

public class DirectoryDeletionException extends GenericApplicationException {
    public DirectoryDeletionException(String message) {
        super(message);
    }

    public DirectoryDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
