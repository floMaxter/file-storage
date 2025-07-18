package com.projects.filestorage.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


public class MinioUtilsTest {

    @ParameterizedTest(name = "extractResourceName(\"{0}\" => \"{1}\"")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#resourceNameExtractionData")
    public void extractResourceName_EmptyPath_ShouldReturnEmptyName(String path, String expectedName) {
        var extractResourceName = MinioUtils.extractResourceName(path);
        assertThat(extractResourceName).isEqualTo(expectedName);
    }

    @ParameterizedTest(name = "extractParentPath(\"{0}\" => \"{1}\"")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#parentPathExtractionData")
    public void extractParentPath_ShouldReturnExpectedParent(String path, String expectedParent) {
        var extractParentPath = MinioUtils.extractParentPath(path);
        assertThat(extractParentPath).isEqualTo(expectedParent);
    }
}
