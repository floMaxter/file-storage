package com.projects.filestorage.utils;

import io.minio.errors.ErrorResponseException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MinioUtils {

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
}
