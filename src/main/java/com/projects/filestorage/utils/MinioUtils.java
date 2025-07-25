package com.projects.filestorage.utils;

import io.minio.errors.ErrorResponseException;
import lombok.experimental.UtilityClass;

import java.nio.file.Paths;
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
        return Paths.get(path).getFileName().toString();
    }

    public String extractParentPath(String path) {
        if (path == null || path.isBlank()) return "";
        return Paths.get(path).getParent().toString();
    }

    public boolean isPathDirectoryLike(String path) {
        return path != null && path.endsWith("/");
    }

    public boolean isNoSuchKey(ErrorResponseException ex) {
        return ex.errorResponse().code().equals("NoSuchKey");
    }

    public boolean isValidPathFormat(String path) {
        return path.matches(String.valueOf(VALID_PATH_PATTERN));
    }

    public boolean isValidDirectoryPathFormat(String path) {
        return path.matches(String.valueOf(VALID_DIRECTORY_PATH_PATTERN));
    }

    public boolean isValidSearchQueryFormat(String query) {
        return query.matches(String.valueOf(VALID_SEARCH_QUERY_PATTERN));
    }
}
