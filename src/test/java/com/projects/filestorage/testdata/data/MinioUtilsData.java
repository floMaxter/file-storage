package com.projects.filestorage.testdata.data;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class MinioUtilsData {

    public static Stream<Arguments> resourceNameExtractionData() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("1.txt", "1.txt"),
                Arguments.of("test/", "test/"),
                Arguments.of("test/1.txt", "1.txt"),
                Arguments.of("test/inner/", "inner/"),
                Arguments.of("test/inner/2.txt", "2.txt")
        );
    }

    public static Stream<Arguments> parentPathExtractionData() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("1.txt", ""),
                Arguments.of("test/", ""),
                Arguments.of("test/1.txt", "test/"),
                Arguments.of("test/inner/", "test/"),
                Arguments.of("test/inner/2.txt", "test/inner/")
        );
    }
}
