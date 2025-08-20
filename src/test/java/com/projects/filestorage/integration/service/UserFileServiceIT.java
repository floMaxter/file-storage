package com.projects.filestorage.integration.service;

import com.projects.filestorage.config.properties.MinioClientProperties;
import com.projects.filestorage.domain.User;
import com.projects.filestorage.exception.DirectoryNotFoundException;
import com.projects.filestorage.exception.ResourceAlreadyExistsException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.repository.MinioRepository;
import com.projects.filestorage.service.UserFileService;
import com.projects.filestorage.service.UserService;
import com.projects.filestorage.testdata.data.dto.TestResource;
import com.projects.filestorage.testdata.data.dto.UploadedTestResource;
import com.projects.filestorage.testutil.TestResourceFactory;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.projects.filestorage.integration.service.TestConfig.Minio;
import static com.projects.filestorage.integration.service.TestConfig.minio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class UserFileServiceIT {

    private User testUser;

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add(Minio.PROP_MINIO_ENDPOINT,
                () -> Minio.MINIO_HTTP_PROTOCOL + minio.getHost() + ":" + minio.getFirstMappedPort()
        );
        registry.add(Minio.PROP_MINIO_ACCESS_KEY, () -> Minio.MINIO_ACCESS_KEY);
        registry.add(Minio.PROP_MINIO_SECRET_KEY, () -> Minio.MINIO_SECRET_KEY);
        registry.add(Minio.PROP_MINIO_BUCKET_NAME, () -> Minio.MINIO_BUCKET_NAME);
    }

    private final UserFileService userFileService;
    private final UserService userService;
    private final MinioRepository minioRepository;
    private final MinioClientProperties minioClientProperties;
    private final TestResourceFactory testResourceFactory;

    @BeforeEach
    void setupBucket() throws Exception {
        var minioClient = MinioClient.builder()
                .endpoint(minioClientProperties.getEndpoint())
                .credentials(minioClientProperties.getAccessKey(), minioClientProperties.getSecretKey())
                .build();

        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioClientProperties.getBucketName())
                .build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .build());
        }

        minioClient.close();
    }

    @BeforeEach
    void setTestUser() {
        testUser = userService.createUser(Minio.MINI0_TEST_USERNAME, Minio.MINIO_TEST_PASSWORD);
    }

    @ParameterizedTest(name = "Get info for existed resource: {0}")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#getValidTestResources")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void getResourceInfo_WhenResourceExists_ShouldReturnCorrectInfo(TestResource testResource) {
        // given
        var expectedResource = testResourceFactory.uploadTestResource(testUser.getId(), testResource);

        // when
        var actualResource = userFileService.getResourceInfo(testUser.getId(), testResource.relativePath());

        // then
        assertThat(actualResource)
                .usingRecursiveComparison()
                .isEqualTo(expectedResource);
    }

    @ParameterizedTest(name = "Get info about an unloaded resource: {0}")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#getValidTestResources")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void getResourceInfo_WhenResourceDoesNotExist_ShouldThrownResourceNotFoundException(TestResource testResource) {
        assertThatThrownBy(() -> userFileService.getResourceInfo(testUser.getId(), testResource.relativePath()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest(name = "Get info about existed directory: {0}")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#getDirectoriesWithResources")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void getDirectoryInfo_WhenDirectoryExists_ShouldReturnNestedResourcesInfo(String directory,
                                                                              List<TestResource> testResources) {
        // given
        var expectedDirectoryResources = testResourceFactory.uploadTestResources(testUser.getId(), testResources);

        // when
        var actualDirectoryInfo = userFileService.getDirectoryInfo(testUser.getId(), directory);

        // then
        assertThat(actualDirectoryInfo)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedDirectoryResources);
    }

    @ParameterizedTest(name = "Get info about unexisted directory: {0}")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#getDirectoryName")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void getDirectoryInfo_WhenDirectoryDoesNotExist_ShouldReturnNestedResourcesInfo(TestResource directory) {
        assertThatThrownBy(() -> userFileService.getDirectoryInfo(testUser.getId(), directory.relativePath()))
                .isInstanceOf(DirectoryNotFoundException.class);
    }

    @ParameterizedTest(name = "Find resources by query: {0}. All resources are suitable for the query.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#searchResourcesValidTestData")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void searchResources_WhenAllNamesContainQuery_ShouldReturnMatchingResources(String query,
                                                                                List<TestResource> resources) {
        // given
        var expectedResources = testResourceFactory.uploadTestResources(testUser.getId(), resources);

        // when
        var actualResources = userFileService.searchResources(testUser.getId(), query);

        // then
        assertThat(actualResources)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedResources);
    }

    @ParameterizedTest(name = "Find resources by query: {0}. No resource is suitable for the query.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#searchResourcesInvalidTestData")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void searchResources_WhenNoNameContainsQuery_ShouldReturnEmptyList(String query,
                                                                       List<TestResource> resources) {
        // given
        var expectedResources = testResourceFactory.uploadTestResources(testUser.getId(), resources);

        // when
        var actualResources = userFileService.searchResources(testUser.getId(), query);

        // then
        assertThat(actualResources)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isNotEqualTo(expectedResources);
    }

    @ParameterizedTest(name = "Create empty directory by valid path: {0}.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#createEmptyDirectoryTestDirectoryPath")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void createEmptyDirectory_WhenPathValid_ShouldReturnEmptyDirectoryInfo(String relativePath) {
        // given
        testResourceFactory.createParentPath(testUser.getId(), relativePath);

        // when
        var emptyDirectoryInfo = userFileService.createEmptyDirectory(testUser.getId(), relativePath);

        // then
        assertThat(emptyDirectoryInfo)
                .isEmpty();
    }

    @ParameterizedTest(name = "Create empty directory by path: {0}. Parent path does not exist.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#createEmptyDirectoryTestDirectoryPath")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void createEmptyDirectory_WhenParentPathDoesNotExist_ShouldThrowDirectoryNotFoundException(String relativePath) {
        assertThatThrownBy(() -> userFileService.createEmptyDirectory(testUser.getId(), relativePath))
                .isInstanceOf(DirectoryNotFoundException.class);
    }

    @ParameterizedTest(name = "Create empty directory by path: {0}. Directory already exists.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#createEmptyDirectoryTestDirectoryPath")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void createEmptyDirectory_WhenDirectoryAlreadyExists_ShouldThrowResourceAlreadyExistsException(String relativePath) {
        testResourceFactory.createDirectory(testUser.getId(), relativePath);

        assertThatThrownBy(() -> userFileService.createEmptyDirectory(testUser.getId(), relativePath))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void createUserRootDirectory_WhenDirectoryDoesNotExist_ShouldCreateRootDirectory() {
        // given
        var userRootDirectory = MinioUtils.buildUserRootPath(testUser.getId());

        // when
        userFileService.createUserRootDir(testUser.getId());

        // then
        assertThat(minioRepository.isDirectoryExists(minioClientProperties.getBucketName(), userRootDirectory))
                .isTrue();
    }

    @Test
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void createUserRootDirectory_WhenDirectoryAlreadyExists_ShouldThrowResourceAlreadyExistsException() {
        var userRootDirectory = MinioUtils.buildUserRootPath(testUser.getId());
        minioRepository.putEmptyDirectory(minioClientProperties.getBucketName(), userRootDirectory);

        assertThatThrownBy(() -> userFileService.createUserRootDir(testUser.getId()))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @ParameterizedTest(name = "Move file {1} to destinationPath={0}. Both arguments are valid.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#moveResourceValidTestFileData")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void moveResource_WhenValidSourceFileAndDestinationPath_ShouldReturnValidResourceInfo(String destinationPath,
                                                                                          TestResource sourceResource) {
        // given
        var uploadedResource = testResourceFactory.uploadTestResource(testUser.getId(), sourceResource);
        var expectedResourceInfo = new UploadedTestResource(
                MinioUtils.extractParentPath(destinationPath),
                MinioUtils.extractResourceName(destinationPath),
                uploadedResource.size(),
                uploadedResource.resourceType()
        );

        // when
        var actualResourceInfo = userFileService.moveResource(testUser.getId(), sourceResource.relativePath(), destinationPath);

        // then
        assertThat(actualResourceInfo)
                .usingRecursiveComparison()
                .isEqualTo(expectedResourceInfo);
    }

    @ParameterizedTest(name = "Move directory {1} to destinationPath={0}. Both arguments are valid.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#moveResourceValidTestDirectoryData")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void moveResource_WhenValidSourceDirectoryAndDestinationPath_ShouldReturnValidResourceInfo(String destinationPath,
                                                                                               TestResource sourceResource,
                                                                                               List<TestResource> innerResources) {
        // given
        var uploadedDirectory = testResourceFactory.uploadTestResource(testUser.getId(), sourceResource);
        var uploadedInnerResources = testResourceFactory.uploadTestResources(testUser.getId(), innerResources);

        var expectedResourceInfo = new UploadedTestResource(
                MinioUtils.extractParentPath(destinationPath),
                MinioUtils.extractResourceName(destinationPath),
                uploadedDirectory.size(),
                uploadedDirectory.resourceType()
        );
        var expectedInnerResourcesInfo = uploadedInnerResources.stream()
                .map(innerResource -> {
                    var sourceAbsolutePath = innerResource.parentPath() + innerResource.name();
                    var relativePath = sourceAbsolutePath.substring(sourceResource.relativePath().length());
                    var destinationAbsolutePath = destinationPath + relativePath;

                    return new UploadedTestResource(
                            MinioUtils.extractParentPath(destinationAbsolutePath),
                            MinioUtils.extractResourceName(destinationAbsolutePath),
                            innerResource.size(),
                            innerResource.resourceType()
                    );
                })
                .toList();

        // when
        var actualResourceInfo = userFileService.moveResource(
                testUser.getId(),
                sourceResource.relativePath(),
                destinationPath
        );
        var actualInnerResourceInfo = userFileService.getDirectoryInfo(testUser.getId(), destinationPath);

        // then
        assertAll(
                () -> assertThat(actualResourceInfo)
                        .usingRecursiveComparison()
                        .isEqualTo(expectedResourceInfo),
                () -> assertThat(actualInnerResourceInfo)
                        .usingRecursiveComparison()
                        .ignoringCollectionOrder()
                        .isEqualTo(expectedInnerResourcesInfo)
        );
    }

    @ParameterizedTest(name = "Move resource {1} to destinationPath={0}. Resource does not exist.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#moveResourceSourceResourceAndDestinationPath")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void moveResource_WhenResourceDoesNotExist_ShouldThrowResourceNotFoundException(String destinationPath,
                                                                                    TestResource sourceResource) {
        assertThatThrownBy(() -> userFileService.moveResource(testUser.getId(), sourceResource.relativePath(), destinationPath))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest(name = "Move resource {1} to destinationPath={0}. Resource already exist on the destination path.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#moveResourceSourceResourceAlreadyExistsAndDestinationPath")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void moveResource_WhenResourceAlreadyExists_ShouldThrownResourceAlreadyExistsException(String destinationPath,
                                                                                           TestResource sourceResource,
                                                                                           TestResource destinationResource) {
        testResourceFactory.uploadTestResource(testUser.getId(), sourceResource);
        testResourceFactory.uploadTestResource(testUser.getId(), destinationResource);

        assertThatThrownBy(() -> userFileService.moveResource(testUser.getId(), sourceResource.relativePath(), destinationPath))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @ParameterizedTest(name = "Download file from path={0}. File exists.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#downloadResourceValidFilePathAndMultipartFiles")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void downloadResource_WhenFileExists_ShouldReturnResourceDownloadDto(String relativePath,
                                                                         MockMultipartFile object) throws IOException {
        // given
        var relativeParentPath = MinioUtils.extractParentPath(relativePath);
        var expectedResourceInfo = userFileService.uploadResource(
                testUser.getId(),
                relativeParentPath,
                object
        );

        // when
        var actualResourceDownloadDto = userFileService.downloadResource(testUser.getId(), relativePath);

        // then
        assertThat(actualResourceDownloadDto.fileName())
                .isEqualTo(expectedResourceInfo.name());

        var outputStream = new ByteArrayOutputStream();
        actualResourceDownloadDto.responseBody().writeTo(outputStream);
        var downloadedBytes = outputStream.toByteArray();

        assertThat(downloadedBytes)
                .isEqualTo(object.getBytes());
    }

    @ParameterizedTest(name = "Download directory from path={0}. Directory exists.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#downloadResourceValidDirectoryPathAndListOfInnerResources")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void downloadResource_WhenDirectoryExists_ShouldReturnResourceDownloadDto(String relativePath,
                                                                              List<MockMultipartFile> objects) throws IOException {
        // given
        objects.forEach(object -> userFileService.uploadResource(testUser.getId(), relativePath, object));

        var expectedDirectoryName = Paths.get(relativePath).getFileName().toString() + ".zip";

        // when
        var actualResourceDownloadDto = userFileService.downloadResource(testUser.getId(), relativePath);

        // then
        assertThat(actualResourceDownloadDto.fileName())
                .isEqualTo(expectedDirectoryName);

        var outputStream = new ByteArrayOutputStream();
        actualResourceDownloadDto.responseBody().writeTo(outputStream);
        var downloadedBytes = outputStream.toByteArray();

        try (var zipInputStream = new ZipInputStream(new ByteArrayInputStream(downloadedBytes))) {
            var extractedFiles = new HashMap<String, byte[]>();
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                var baos = new ByteArrayOutputStream();
                zipInputStream.transferTo(baos);
                extractedFiles.put(entry.getName(), baos.toByteArray());
            }

            assertThat(extractedFiles.keySet())
                    .containsExactlyInAnyOrderElementsOf(
                            objects.stream()
                                    .map(MockMultipartFile::getOriginalFilename)
                                    .toList()
                    );

            for (var expected : objects) {
                assertThat(extractedFiles.get(expected.getOriginalFilename()))
                        .isEqualTo(expected.getBytes());
            }
        }
    }

    @ParameterizedTest(name = "Download resource {0}. Resource does not exist.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#getValidTestResources")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void downloadResource_WhenResourceDoesNotExist_ShouldThrowResourceNotFoundException(TestResource testResource) {
        assertThatThrownBy(() -> userFileService.downloadResource(testUser.getId(), testResource.relativePath()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest(name = "Upload valid resource {1} by path={0}")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#uploadResourceValidPathAndMultipartFile")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void uploadResource_WhenValidResource_ShouldReturnCorrectInfo(String relativeDirPath,
                                                                  MockMultipartFile object) throws Exception {
        // given
        var userRootDir = MinioUtils.buildUserRootPath(testUser.getId());
        var relativeResourcePath = relativeDirPath + object.getOriginalFilename();
        var absoluteResourcePath = MinioUtils.getAbsolutePath(userRootDir, relativeResourcePath);

        // when
        var actualResourceInfo = userFileService.uploadResource(testUser.getId(), relativeDirPath, object);

        // then
        assertThat(actualResourceInfo)
                .extracting(
                        ResourceInfoResponseDto::parentPath,
                        ResourceInfoResponseDto::name,
                        ResourceInfoResponseDto::size)
                .containsExactly(
                        relativeDirPath,
                        object.getOriginalFilename(),
                        object.getSize()
                );

        try (var inputStream = minioRepository.getObject(minioClientProperties.getBucketName(), absoluteResourcePath)) {
            var downloadedBytes = inputStream.readAllBytes();
            assertThat(downloadedBytes).isEqualTo(object.getBytes());
        }
    }

    @ParameterizedTest(name = "Upload resource {1} by path={0}. Resource already exists.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#uploadResourceValidPathAndMultipartFile")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void uploadResource_WhenResourceAlreadyExists_ShouldThrowResourceAlreadyExistsException(String relativeDirPath,
                                                                                           MockMultipartFile object) {
        // given
        var userRootPath = MinioUtils.buildUserRootPath(testUser.getId());
        var relativeResourcePath = relativeDirPath + object.getOriginalFilename();
        var absoluteResourcePath = MinioUtils.getAbsolutePath(userRootPath, relativeResourcePath);

        minioRepository.uploadResource(minioClientProperties.getBucketName(), absoluteResourcePath, object);

        // then
        assertThatThrownBy(() -> userFileService.uploadResource(testUser.getId(), relativeDirPath, object))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @ParameterizedTest(name = "Upload valid resources in directory by path={0}")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#uploadResourcesValidPathAndMultipartFiles")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void uploadResources_WhenValidResources_ShouldReturnListCorrectInfo(String relativeDirPath,
                                                                        List<MultipartFile> objects) throws Exception {
        // given
        var userRootDir = MinioUtils.buildUserRootPath(testUser.getId());
        var expectedResourceInfos = objects.stream()
                .map(obj -> {
                    var relativeResourcePath = relativeDirPath + obj.getOriginalFilename();
                    var relativeParentPath = MinioUtils.extractParentPath(relativeResourcePath);

                    return new ResourceInfoResponseDto(
                            relativeParentPath,
                            obj.getOriginalFilename(),
                            obj.getSize(),
                            ResourceType.FILE
                    );
                })
                .toList();

        // when
        var actualResourceInfos = userFileService.uploadResources(testUser.getId(), relativeDirPath, objects);

        // then
        assertThat(actualResourceInfos)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedResourceInfos);

        for (var object : objects) {
            var relativePath = relativeDirPath + object.getOriginalFilename();
            var absolutePath = MinioUtils.getAbsolutePath(userRootDir, relativePath);

            try (var inputStream = minioRepository.getObject(minioClientProperties.getBucketName(), absolutePath)) {
                var downloadedBytes = inputStream.readAllBytes();
                assertThat(downloadedBytes).isEqualTo(object.getBytes());
            }
        }
    }

    @ParameterizedTest(name = "Upload valid resources in directory by path={0}. Resources already exist.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#uploadResourcesValidPathAndMultipartFiles")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void uploadResources_WhenResourcesAlreadyExist_ShouldThrowResourceAlreadyExistsException(String relativeDirPath,
                                                                                             List<MultipartFile> objects) {
        // given
        var userRootDir = MinioUtils.buildUserRootPath(testUser.getId());
        objects.forEach(obj -> {
            var relativePath = relativeDirPath + obj.getOriginalFilename();
            var absolutePath = MinioUtils.getAbsolutePath(userRootDir, relativePath);

            minioRepository.uploadResource(minioClientProperties.getBucketName(), absolutePath, obj);
        });

        // then
        assertThatThrownBy(() -> userFileService.uploadResources(testUser.getId(), relativeDirPath, objects))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @ParameterizedTest(name = "Delete resource by path={0}. Resource exists.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#deleteResourceValidTestResources")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void deleteResource_WhenResourceExists_ShouldDeleteResource(TestResource testResource) {
        // given
        var userRootDir = MinioUtils.buildUserRootPath(testUser.getId());
        var parentPath = MinioUtils.extractParentPath(testResource.relativePath());
        var absoluteParentPath = MinioUtils.getAbsolutePath(userRootDir, parentPath);

        testResourceFactory.uploadTestResource(testUser.getId(), testResource);

        // when
        userFileService.deleteResource(testUser.getId(), testResource.relativePath());

        // then
        deleteResource_AssertNotExists(testResource);
        assertThat(minioRepository.isDirectoryExists(minioClientProperties.getBucketName(), absoluteParentPath))
                .isTrue();
    }

    @ParameterizedTest(name = "Delete resource by path={0}. Resource does not exist.")
    @MethodSource("com.projects.filestorage.testdata.data.MinioTestData#deleteResourceValidTestResources")
    @WithMockUser(username = Minio.MINI0_TEST_USERNAME, password = Minio.MINIO_TEST_PASSWORD)
    @Transactional
    @Rollback
    void deleteResource_WhenResourceDoesNotExist_ShouldThrowResourceNotFoundException(TestResource testResource) {
        assertThatThrownBy(() -> userFileService.deleteResource(testUser.getId(), testResource.relativePath()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    void deleteResource_AssertNotExists(TestResource resource) {
        if (resource.isDirectory()) {
            assertThat(minioRepository.isDirectoryExists(minioClientProperties.getBucketName(), resource.relativePath()))
                    .isFalse();
        } else {
            assertThat(minioRepository.isFileExists(minioClientProperties.getBucketName(), resource.relativePath()))
                    .isFalse();
        }
    }
}
