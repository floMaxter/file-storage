package com.projects.filestorage.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class MinioUtils {

    public String extractResourceName(String path) {
        var resourceName = Arrays.asList(path.split("/")).getLast();
        return isFile(path) ? resourceName : resourceName.concat("/");
    }

    public String extractPath(String path) {
        var splitPath = new ArrayList<>(List.of(path.split("/")));
        if (splitPath.isEmpty()) {
            return "/";
        }

        splitPath.removeLast();

        var joinedPath = String.join("/", splitPath);
        return joinedPath.isBlank() ? "/" : joinedPath + "/";
    }

    public boolean isFile(String path) {
        return !path.endsWith("/");
    }

    public boolean isDirectory(String path) {
        return path.endsWith("/");
    }
}
