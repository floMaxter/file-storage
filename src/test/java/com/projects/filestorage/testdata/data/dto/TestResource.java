package com.projects.filestorage.testdata.data.dto;

public record TestResource(String relativePath, String content, boolean isDirectory) {


    public static TestResource file(String relativePath, String content) {
        return new TestResource(relativePath, content, false);
    }

    public static TestResource directory(String relativePath) {
        return new TestResource(relativePath, null, true);
    }
}
