package com.projects.filestorage.exception;

public class MinioAccessException extends GenericApplicationException {
    public MinioAccessException(String message) {
        super(message);
    }

    public MinioAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
