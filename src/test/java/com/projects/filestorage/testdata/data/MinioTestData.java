package com.projects.filestorage.testdata.data;

import com.projects.filestorage.testdata.data.dto.TestResource;
import com.projects.filestorage.testutil.TestUtils;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.stream.Stream;

public class MinioTestData {

    public static Stream<TestResource> getValidTestResources() {
        return Stream.of(
                TestResource.file("1.txt", "Hello from 1.txt"),
                TestResource.file("folder1/1.txt", "Hello folder1/1.txt"),
                TestResource.file("folder1/2.txt", "Hello folder1/2.txt"),
                TestResource.file("folder1/folder2/1.txt", "Hello folder1/folder2/1.txt"),
                TestResource.file("folder1/folder2/folder3/1.txt", "Hello folder1/folder2/folder3/1.txt"),
                TestResource.directory("folder2/"),
                TestResource.directory("folder3/folder4/"),
                TestResource.directory("folder4/folder5/folder6/")
        );
    }

    public static Stream<Arguments> getDirectoriesWithResources() {
        return Stream.of(
                Arguments.of("folder1/",
                        List.of(
                                TestResource.file("folder1/file1.txt", "file1"),
                                TestResource.file("folder1/file2.txt", "file2"),
                                TestResource.file("folder1/file3.txt", "file3"),
                                TestResource.directory("folder1/folder_inner_1/"),
                                TestResource.directory("folder1/folder_inner_2/"),
                                TestResource.directory("folder1/folder_inner_3/")
                        )
                ),
                Arguments.of("folder2/",
                        List.of(
                                TestResource.file("folder2/file1.txt", "file1"),
                                TestResource.file("folder2/file2.txt", "file2"),
                                TestResource.file("folder2/file3.txt", "file3"),
                                TestResource.directory("folder2/folder_inner_1/"),
                                TestResource.directory("folder2/folder_inner_2/"),
                                TestResource.directory("folder2/folder_inner_3/")
                        )
                )
        );
    }

    public static Stream<TestResource> getDirectoryName() {
        return Stream.of(
                TestResource.directory("folder1/"),
                TestResource.directory("folder2/"),
                TestResource.directory("folder3/folder_inner1/"),
                TestResource.directory("folder4/folder_inner1/folder_inner2/")
        );
    }

    public static Stream<Arguments> searchResourcesValidTestData() {
        return Stream.of(
                Arguments.of(
                        "test_name",
                        List.of(
                                TestResource.file("test_name_file.txt", "content"),
                                TestResource.file("test_name_folder/test_name_inner_file.txt", "content"),
                                TestResource.directory("test_name_folder/test_name_inner_folder/"),
                                TestResource.directory("test_name_folder/")
                        )
                ),
                Arguments.of(
                        "report",
                        List.of(
                                TestResource.file("report1.txt", "content"),
                                TestResource.file("monthly_report.docx", "content"),
                                TestResource.file("reports/annual_report.pdf", "content"),
                                TestResource.directory("reports/")
                        )
                ),
                Arguments.of(
                        "logs",
                        List.of(
                                TestResource.directory("logs/"),
                                TestResource.file("folder/app_logs.log", "content"),
                                TestResource.file("logs/error_app_logs.log", "content"),
                                TestResource.file("archive_logs/old.logs", "content"),
                                TestResource.directory("archive_logs/")
                        )
                ),
                Arguments.of(
                        ".jpg",
                        List.of(
                                TestResource.file("photo.jpg", "binary"),
                                TestResource.file("images/holiday.jpg", "binary")
                        )
                ),
                Arguments.of(
                        "draft",
                        List.of(
                                TestResource.file("draft.txt", "content"),
                                TestResource.file("project_draft_v2.docx", "content"),
                                TestResource.file("notes_draft.pdf", "content"),
                                TestResource.directory("drafts/")
                        )
                ),
                Arguments.of(
                        "1",
                        List.of(
                                TestResource.file("file1.txt", "content"),
                                TestResource.file("folder/note_1_note.txt", "content"),
                                TestResource.file("folder2/file21.txt", "content"),
                                TestResource.directory("folder1/")
                        )
                )
        );
    }

    public static Stream<Arguments> searchResourcesInvalidTestData() {
        return Stream.of(
                Arguments.of(
                        "report",
                        List.of(
                                TestResource.file("rep_ort.txt", "content"),
                                TestResource.file("monthly_repor.docx", "content"),
                                TestResource.file("reports/annual.pdf", "content"),
                                TestResource.directory("report/inner_folder/")
                        )
                ),
                Arguments.of(
                        "logs",
                        List.of(
                                TestResource.file("logs/app.log", "content"),
                                TestResource.file("logs/error.log", "content"),
                                TestResource.file("archive_logs/old.log", "content"),
                                TestResource.directory("archive_logs/logger/")
                        )
                ),
                Arguments.of(
                        ".jpg",
                        List.of(
                                TestResource.file("jpg/photo.png", "binary"),
                                TestResource.file("avatar.JPG", "binary"),
                                TestResource.file("images/holiday.png", "binary"),
                                TestResource.file("images/readme.txt", "content")
                        )
                ),
                Arguments.of(
                        "draft",
                        List.of(
                                TestResource.file("draft/photo.txt", "content"),
                                TestResource.file("project_v2.docx", "content"),
                                TestResource.file("notes_drafft.pdf", "content"),
                                TestResource.directory("drafts/inner_folder/")
                        )
                )
        );
    }

    public static Stream<String> createEmptyDirectoryTestDirectoryPath() {
        return Stream.of(
                "folder1/",
                "folder2/",
                "folder3/",
                "folder1/inner_folder1/",
                "folder2/inner_folder1/inner_folder2/",
                "folder3/inner_folder1/inner_folder2/inner_folder3/"
        );
    }

    public static Stream<Arguments> moveResourceValidTestFileData() {
        return Stream.of(
                Arguments.of(
                        "move_folder1/file.txt",
                        TestResource.file("folder1/file.txt", "content")
                ),
                Arguments.of(
                        "folder2/rename_file.txt",
                        TestResource.file("folder2/file.txt", "content")
                ),
                Arguments.of(
                        "folder3/inner_folder2/file.txt",
                        TestResource.file("folder3/inner_folder1/file.txt", "content")
                )
        );
    }

    public static Stream<Arguments> moveResourceValidTestDirectoryData() {
        return Stream.of(
                Arguments.of(
                        "move_folder1/",
                        TestResource.directory("folder1/"),
                        List.of(
                                TestResource.file("folder1/file1.txt", "content"),
                                TestResource.file("folder1/file2.txt", "content"),
                                TestResource.file("folder1/file3.txt", "content"),
                                TestResource.directory("folder1/inner_folder1/"),
                                TestResource.directory("folder1/inner_folder2/"),
                                TestResource.directory("folder1/inner_folder3/")
                        )
                ),
                Arguments.of(
                        "folder2/move_inner_folder1/",
                        TestResource.directory("folder1/inner_folder1/"),
                        List.of(
                                TestResource.file("folder1/inner_folder1/file1.txt", "content"),
                                TestResource.file("folder1/inner_folder1/file2.txt", "content"),
                                TestResource.file("folder1/inner_folder1/file3.txt", "content"),
                                TestResource.directory("folder1/inner_folder1/inner_folder1_1/"),
                                TestResource.directory("folder1/inner_folder1/inner_folder1_2/"),
                                TestResource.directory("folder1/inner_folder1/inner_folder1_3/")
                        )
                )
        );
    }

    public static Stream<Arguments> moveResourceSourceResourceAndDestinationPath() {
        return Stream.of(
                Arguments.of(
                        "move_folder1/file.txt",
                        TestResource.file("folder1/file.txt", "content")
                ),
                Arguments.of(
                        "folder2/rename_file.txt",
                        TestResource.file("folder2/file.txt", "content")
                ),
                Arguments.of(
                        "folder3/inner_folder2/file.txt",
                        TestResource.file("folder3/inner_folder1/file.txt", "content")
                ),
                Arguments.of(
                        "move_folder1/",
                        TestResource.directory("folder1/")
                ),
                Arguments.of(
                        "folder2/move_inner_folder1/",
                        TestResource.directory("folder1/inner_folder1/")
                )
        );
    }

    public static Stream<Arguments> moveResourceSourceResourceAlreadyExistsAndDestinationPath() {
        return Stream.of(
                Arguments.of(
                        "move_folder1/file.txt",
                        TestResource.file("folder1/file.txt", "content"),
                        TestResource.file("move_folder1/file.txt", "content")
                ),
                Arguments.of(
                        "folder2/rename_file.txt",
                        TestResource.file("folder2/file.txt", "content"),
                        TestResource.file("folder2/rename_file.txt", "content")
                ),
                Arguments.of(
                        "folder3/inner_folder2/file.txt",
                        TestResource.file("folder3/inner_folder1/file.txt", "content"),
                        TestResource.file("folder3/inner_folder2/file.txt", "content")
                ),
                Arguments.of(
                        "move_folder1/",
                        TestResource.directory("folder1/"),
                        TestResource.directory("move_folder1/")
                ),
                Arguments.of(
                        "folder2/move_inner_folder1/",
                        TestResource.directory("folder1/inner_folder1/"),
                        TestResource.directory("folder2/move_inner_folder1/")
                )
        );
    }

    public static Stream<Arguments> uploadResourceValidPathAndMultipartFile() {
        return Stream.of(
                Arguments.of("folder1/", mockFile("1.txt", "Content from folder1/1.txt")),
                Arguments.of("folder1/inner_folder1/", mockFile("2.txt", "Content from folder1/inner_folder1/2.txt")),
                Arguments.of("folder1/inner_folder1/inner_folder2/", mockFile("3.txt", "Content from folder1/inner_folder1/inner_folder2/3.txt"))
        );
    }

    public static Stream<Arguments> uploadResourcesValidPathAndMultipartFiles() {
        return Stream.of(
                Arguments.of(
                        "folder1/",
                        List.of(
                                mockFile("1.txt", "Content from folder1/1.txt"),
                                mockFile("2.txt", "Content from folder1/2.txt"),
                                mockFile("3.txt", "Content from folder1/3.txt")
                        )
                ),
                Arguments.of(
                        "folder1/inner_folder1/",
                        List.of(
                                mockFile("1.txt", "Content from folder1/inner_folder1/1.txt"),
                                mockFile("2.txt", "Content from folder1/inner_folder1/2.txt"),
                                mockFile("3.txt", "Content from folder1/inner_folder1/3.txt")
                        )
                ),
                Arguments.of(
                        "folder1/inner_folder1/inner_folder2/",
                        List.of(
                                mockFile("1.txt", "Content from folder1/inner_folder1/inner_folder2/1.txt"),
                                mockFile("2.txt", "Content from folder1/inner_folder1/inner_folder2/2.txt"),
                                mockFile("3.txt", "Content from folder1/inner_folder1/inner_folder2/3.txt")
                        )
                )
        );
    }

    public static Stream<Arguments> downloadResourceValidFilePathAndMultipartFiles() {
        return Stream.of(
                Arguments.of("folder1/1.txt", mockFile("1.txt", "Content from folder1/1.txt")),
                Arguments.of("folder1/inner_folder1/2.txt", mockFile("2.txt", "Content from folder1/inner_folder1/2.txt")),
                Arguments.of("folder1/inner_folder1/inner_folder2/3.txt", mockFile("3.txt", "Content from folder1/inner_folder1/inner_folder2/3.txt"))
        );
    }

    public static Stream<Arguments> downloadResourceValidDirectoryPathAndListOfInnerResources() {
        return Stream.of(
                Arguments.of(
                        "folder1/",
                        List.of(
                                mockFile("file1.txt", "Hello from file1.txt"),
                                mockFile("file2.txt", "Hello from file2.txt"),
                                mockFile("inner_folder/file3.txt", "Hello from inner_folder/file3.txt")
                        )
                )
        );
    }

    public static Stream<TestResource> deleteResourceValidTestResources() {
        return Stream.of(
                TestResource.file("parent_directory/1.txt", "Hello from parent_directory/1.txt"),
                TestResource.file("folder1/1.txt", "Hello folder1/1.txt"),
                TestResource.file("folder1/2.txt", "Hello folder1/2.txt"),
                TestResource.file("folder1/folder2/1.txt", "Hello folder1/folder2/1.txt"),
                TestResource.file("folder1/folder2/folder3/1.txt", "Hello folder1/folder2/folder3/1.txt"),
                TestResource.directory("parent_directory/folder2/"),
                TestResource.directory("folder3/folder4/"),
                TestResource.directory("folder4/folder5/folder6/")
        );
    }

    private static MockMultipartFile mockFile(String fileName, String content) {
        return new MockMultipartFile(
                TestUtils.MULTIPART_FORM_FIELD_NAME,
                fileName,
                TestUtils.MULTIPART_CONTENT_TYPE,
                content.getBytes()
        );
    }
}
