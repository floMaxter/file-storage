package com.projects.filestorage.validation;

import com.projects.filestorage.exception.InvalidResourcePathFormatException;
import com.projects.filestorage.exception.InvalidSearchQueryFormatException;
import com.projects.filestorage.integration.service.TestConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestConfig.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ResourcePathValidatorTest {

    private final ResourcePathValidator resourcePathValidator;

    @ParameterizedTest(name = "Validate the correct path: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.ResourcePathTestData#getValidPath")
    public void validPathFormat_ValidPath_ShouldNotThrowException(String path) {
        assertThatCode(() -> resourcePathValidator.validatePathFormat(path))
                .doesNotThrowAnyException();
    }


    @ParameterizedTest(name = "Validate the incorrect path: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.ResourcePathTestData#getInvalidPath")
    public void validPathFormat_InvalidPath_ShouldThrowInvalidResourcePathFormatException(String path) {
        assertThatThrownBy(() -> resourcePathValidator.validatePathFormat(path))
                .isInstanceOf(InvalidResourcePathFormatException.class);
    }

    @ParameterizedTest(name = "Validate the correct directory path: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.ResourcePathTestData#getValidDirectoryPath")
    public void validDirectoryPathFormat_ValidPath_ShouldNotThrowException(String path) {
        assertThatCode(() -> resourcePathValidator.validateDirectoryPathFormat(path))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "Validate the incorrect directory path: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.ResourcePathTestData#getInvalidDirectoryPath")
    public void validDirectoryPathFormat_InvalidPath_ShouldThrowInvalidResourcePathFormatException(String path) {
        assertThatThrownBy(() -> resourcePathValidator.validateDirectoryPathFormat(path))
                .isInstanceOf(InvalidResourcePathFormatException.class);
    }

    @ParameterizedTest(name = "Validate the correct search query: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.ResourcePathTestData#getValidSearchQuery")
    public void validSearchQueryFormat_ValidQuery_ShouldNotThrowException(String query) {
        assertThatCode(() -> resourcePathValidator.validateSearchQueryFormat(query))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "Validate the incorrect search query: (\"{0}\")")
    @MethodSource("com.projects.filestorage.testdata.data.ResourcePathTestData#getInvalidSearchQuery")
    public void validSearchQueryFormat_InvalidQuery_ShouldThrowInvalidSearchQueryFormatException(String query) {
        assertThatThrownBy(() -> resourcePathValidator.validateSearchQueryFormat(query))
                .isInstanceOf(InvalidSearchQueryFormatException.class);
    }
}
