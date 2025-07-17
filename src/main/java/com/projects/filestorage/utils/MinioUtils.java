package com.projects.filestorage.utils;

import io.minio.errors.ErrorResponseException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MinioUtils {

    public String extractResourceName(String path) {
        var lastSlash = path.lastIndexOf("/");
        if (lastSlash == -1 || lastSlash == path.length() - 1) return "";
        return path.substring(lastSlash + 1);
    }

    public String extractPath(String path) {
        var lastSlash = path.lastIndexOf("/");
        if (lastSlash <= 0) return "/";
        return path.substring(0, lastSlash + 1);
    }

    public boolean isNoSuchKey(ErrorResponseException ex) {
        return ex.errorResponse().code().equals("NoSuchKey");
    }
}
