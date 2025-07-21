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

    public static Stream<String> getValidPath() {
        return Stream.of(
                "",
                "_",
                ".",
                "-",
                "file",
                "файл",
                "file.txt",
                "файл.txt",
                "directory/",
                "папка/",
                "directory/file",
                "папка/файл",
                "directory/file.txt",
                "папка/файл.txt",
                "directory/inner-directory/",
                "папка/вложенная-папка/",
                "directory/inner-directory/file",
                "папка/вложенная-папка/файл",
                "directory/inner-directory/file.txt",
                "папка/вложенная-папка/файл.txt"
        );
    }

    public static Stream<String> getValidDirectoryPath() {
        return Stream.of(
                "directory/",
                "папка/",
                "directory/inner-directory/",
                "папка/вложенная-папка/"
        );
    }
}
