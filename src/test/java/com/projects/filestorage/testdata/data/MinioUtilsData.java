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
                "-",
                "file",
                "файл",
                "file.txt",
                "файл.txt",
                "directory/",
                "папка/",
                "file.txt/",
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

    public static Stream<String> getInvalidPath() {
        return Stream.of(
                " ",
                "/",
                "//",
                "folder//file.txt",
                "/folder",
                "folder/file?.txt",
                "folder/file*.txt",
                "folder/file|.txt",
                "folder/ file.txt",
                "folder/файл .txt",
                "folder/../file.txt",
                "../file.txt",
                "..",
                "./file.txt",
                "folder//",
                "folder/..",
                "folder/sub/..",
                "folder/файл?.txt"
        );
    }

    public static Stream<String> getValidDirectoryPath() {
        return Stream.of(
                "dir/",
                "папка/",
                "dir-name/",
                "имя_папки/",
                "dir-123/",
                "папка123/",
                "nested/dir/",
                "вложенная/папка/",
                "deep/nested/dir/",
                "глубокая/вложенная/папка/",
                "a/",
                "д/",
                "_dir/",
                ".hidden-dir/",
                "директория-с-тире/",
                "директория_с_подчёркиванием/"
        );
    }

    public static Stream<String> getInvalidDirectoryPath() {
        return Stream.of(
                "../",
                "/",
                "/folder/",
                "folder",
                "folder//",
                "folder/../",
                "folder/..",
                "./folder/",
                "folder/./",
                "folder/sub/..",
                "folder/./sub/",
                "folder/..sub/",
                "folder/sub//",
                "folder/sub\\",
                "folder\\sub/",
                "folder/sub\\",
                "folder\\sub\\",
                "folder/ /",
                " fol der/",
                "folder/ ",
                "fol*der/",
                "fold?er/",
                "folder>/",
                "folder<sub/",
                "folder|sub/",
                "folder\n/",
                "folder\r/"
        );
    }

    public static Stream<String> getValidSearchQuery() {
        return Stream.of(
                "_",
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

    public static Stream<String> getInvalidSearchQuery() {
        return Stream.of(
                "",
                "/",
                "//",
                "folder//file.txt",
                "/folder",
                "folder/file?.txt",
                "folder/file*.txt",
                "folder/file|.txt",
                "folder/ file.txt",
                "folder/файл .txt",
                "folder/../file.txt",
                "../file.txt",
                "..",
                "./file.txt",
                "folder//",
                "folder/..",
                "folder/sub/..",
                "folder/файл?.txt"
        );
    }
}
