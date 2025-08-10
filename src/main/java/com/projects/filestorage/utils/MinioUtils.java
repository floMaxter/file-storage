package com.projects.filestorage.utils;

import io.minio.errors.ErrorResponseException;
import lombok.experimental.UtilityClass;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MinioUtils {

    public String extractResourceName(String path) {
        if (path == null || path.isBlank()) return "";

        var segments = new ArrayList<>(List.of(path.split("/")));
        var lastSegment = segments.getLast();

        return path.endsWith("/") ? lastSegment + "/" : lastSegment;
    }

    public String extractParentPath(String path) {
        if (path == null || path.isBlank()) return "";

        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        var lastSlash = path.lastIndexOf("/");
        if (lastSlash == -1) return "";

        return path.substring(0, lastSlash + 1);
    }

    public String buildUserRootPath(Long userId) {
        return String.format("user-%d-files/", userId);
    }

    public String getAbsolutePath(String relativePath, String userRootDirectory) {
        return userRootDirectory + relativePath;
    }

    public String getRelativePath(String absolutePath, String userRootDirectory) {
        if (!absolutePath.startsWith(userRootDirectory)) {
            throw new IllegalArgumentException("Path does not start with user root directory");
        }
        return absolutePath.substring(userRootDirectory.length());
    }

    public boolean fileNameMatchesQuery(String path, String query) {
        var fileName = Paths.get(path).getFileName().toString();
        return fileName.toLowerCase().contains(query);
    }

    public boolean isPathDirectoryLike(String path) {
        return path != null && path.endsWith("/");
    }

    public boolean isPathFileLike(String path) {
        return path != null && !path.endsWith("/");
    }

    public boolean isNoSuchKey(ErrorResponseException ex) {
        return ex.errorResponse().code().equals("NoSuchKey");
    }
}
