package com.projects.filestorage.utils;

import io.minio.errors.ErrorResponseException;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class MinioUtils {

    private static final Pattern VALID_PATH_PATTERN = Pattern.compile(
            "^(?!.*(?:^|/)\\.\\.?(?:/|$))(?:[\\p{L}\\p{N}_.-]+/)*[\\p{L}\\p{N}_.-]+/?$|^$"
    );
    private static final Pattern VALID_DIRECTORY_PATH_PATTERN = Pattern.compile(
            "^(?!.*(?:^|/)(\\.{1,2})(?:/|$))(?!.*(?:^|/)(\\.\\.[^/]*)(?:/|$))([\\p{L}\\p{N}_.-]+/)+$"
    );
    private static final Pattern VALID_SEARCH_QUERY_PATTERN = Pattern.compile(
            "^(?!.*(?:^|/)(?:\\.|\\.\\.)($|/))(?:[\\p{L}\\p{N}_.-]+/)*[\\p{L}\\p{N}_.-]+/?$"
    );

    public String extractResourceName(String path) {
        if (path == null || path.isBlank()) return "";

        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        var lastSlash = path.lastIndexOf("/");
        if (lastSlash == -1) return path;

        return path.substring(lastSlash + 1);
    }

    public String extractParentPath(String path) {
        if (path == null || path.isBlank()) return "";

        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        var lastSlash = path.lastIndexOf("/");
        if (lastSlash == -1) return "";

        return path.substring(0, lastSlash + 1);
    }

    public boolean isPathDirectoryLike(String path) {
        return path != null && path.endsWith("/");
    }

    public boolean isNoSuchKey(ErrorResponseException ex) {
        return ex.errorResponse().code().equals("NoSuchKey");
    }

    public boolean isValidPathFormat(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return path.matches(String.valueOf(VALID_PATH_PATTERN));
    }

    public boolean isValidDirectoryPathFormat(String path) {
        return path.matches(String.valueOf(VALID_DIRECTORY_PATH_PATTERN));
    }

    public boolean isValidSearchQueryFormat(String query) {
        return query.matches(String.valueOf(VALID_SEARCH_QUERY_PATTERN));
    }
}
