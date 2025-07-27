package com.projects.filestorage.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


public class MinioUtilsTest {

    @ParameterizedTest(name = "extractResourceName(\"{0}\" => \"{1}\"")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#resourceNameExtractionData")
    public void extractResourceName_ValidPath_ShouldReturnEmptyName(String path, String expectedName) {
        var extractResourceName = MinioUtils.extractResourceName(path);
        assertThat(extractResourceName).isEqualTo(expectedName);
    }

    @ParameterizedTest(name = "extractParentPath(\"{0}\" => \"{1}\"")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#parentPathExtractionData")
    public void extractParentPath_ValidPath_ShouldReturnExpectedParent(String path, String expectedParent) {
        var extractParentPath = MinioUtils.extractParentPath(path);
        assertThat(extractParentPath).isEqualTo(expectedParent);
    }

    @ParameterizedTest(name = "Validate the correct path: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#getValidPath")
    public void isValidPathFormat_ValidPath_ShouldReturnTrue(String path) {
        var result = MinioUtils.isValidPathFormat(path);
        assertThat(result).isTrue();
    }

    @ParameterizedTest(name = "Validate the incorrect path: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#getInvalidPath")
    public void isValidPathFormat_InvalidPath_ShouldReturnFalse(String path) {
        var result = MinioUtils.isValidPathFormat(path);
        assertThat(result).isFalse();
    }

    @ParameterizedTest(name = "Validate the correct directory path: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#getValidDirectoryPath")
    public void isValidDirectoryPathFormat_ValidPath_ShouldReturnTrue(String path) {
        var result = MinioUtils.isValidDirectoryPathFormat(path);
        assertThat(result).isTrue();
    }

    @ParameterizedTest(name = "Validate the incorrect directory path: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#getInvalidDirectoryPath")
    public void isValidDirectoryPathFormat_InvalidPath_ShouldReturnFalse(String path) {
        var result = MinioUtils.isValidDirectoryPathFormat(path);
        assertThat(result).isFalse();
    }

    @ParameterizedTest(name = "Validate the correct search query: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#getValidSearchQuery")
    public void isValidSearchQueryFormat_ValidQuery_ShouldReturnTrue(String query) {
        var result = MinioUtils.isValidSearchQueryFormat(query);
        assertThat(result).isTrue();
    }

    @ParameterizedTest(name = "Validate the incorrect search query: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.MinioUtilsData#getInvalidSearchQuery")
    public void isValidSearchQueryFormat_InvalidQuery_ShouldReturnFalse(String query) {
        var result = MinioUtils.isValidSearchQueryFormat(query);
        assertThat(result).isFalse();
    }
}
