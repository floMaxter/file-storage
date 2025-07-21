package com.projects.filestorage.utils;

import io.minio.errors.ErrorResponseException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public class MinioUtils {

    private static final Pattern VALID_PATH_PATTERN = Pattern.compile("^(?:[\\p{L}\\p{N}_.-]+/)*[\\p{L}\\p{N}_.-]+/?$|^$");
    private static final Pattern VALID_DIRECTORY_PATH_PATTERN = Pattern.compile("^(?:[\\p{L}\\p{N}_.-]+/)+$");

    public String extractResourceName(String path) {
        if (path.isBlank()) return "";

        var segments = new ArrayList<>(List.of(path.split("/")));
        if (segments.size() == 1) return path;

        String last = segments.getLast();
        return path.endsWith("/") ? last + "/" : last;
    }

    public String extractParentPath(String path) {
        if (path.isBlank()) return "";

        var segments = new ArrayList<>(List.of(path.split("/")));
        if (segments.size() <= 1) return "";

        segments.removeLast();
        return String.join("/", segments) + "/";
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
}
