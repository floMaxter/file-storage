package com.projects.filestorage.testutil;

import com.projects.filestorage.config.properties.MinioClientProperties;
import com.projects.filestorage.repository.MinioRepository;
import com.projects.filestorage.testdata.data.dto.TestResource;
import com.projects.filestorage.testdata.data.dto.UploadedTestResource;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TestResourceFactory {

    private final MinioRepository minioRepository;
    private final MinioClientProperties minioClientProperties;

    public UploadedTestResource uploadTestResource(Long userId, TestResource testResource) {
        var userRootDirectory = MinioUtils.buildUserRootPath(userId);
        var absolutePath = MinioUtils.getAbsolutePath(userRootDirectory, testResource.relativePath());
        var resourceName = MinioUtils.extractResourceName(testResource.relativePath());
        var relativeParentPath = MinioUtils.extractParentPath(testResource.relativePath());

        if (testResource.isDirectory()) {
            minioRepository.putEmptyDirectory(minioClientProperties.getBucketName(), absolutePath);
            return new UploadedTestResource(relativeParentPath, resourceName, 0L, ResourceType.DIRECTORY);
        } else {
            var file = new MockMultipartFile(
                    TestUtils.MULTIPART_FORM_FIELD_NAME,
                    resourceName,
                    TestUtils.MULTIPART_CONTENT_TYPE,
                    testResource.content().getBytes()
            );

            minioRepository.uploadResource(minioClientProperties.getBucketName(), absolutePath, file);

            return new UploadedTestResource(relativeParentPath, resourceName, file.getSize(), ResourceType.FILE);
        }
    }

    public List<UploadedTestResource> uploadTestResources(Long userId, List<TestResource> testResources) {
        return testResources.stream()
                .map(resource -> uploadTestResource(userId, resource))
                .toList();
    }

    public void createParentPath(Long userId, String relativePath) {
        var userRootDirectory = MinioUtils.buildUserRootPath(userId);
        var relativeParentPath = MinioUtils.extractParentPath(relativePath);
        var absoluteParentPath = MinioUtils.getAbsolutePath(userRootDirectory, relativeParentPath);

        minioRepository.putEmptyDirectory(minioClientProperties.getBucketName(), absoluteParentPath);
    }

    public void createDirectory(Long userId, String relativePath) {
        var userRootDirectory = MinioUtils.buildUserRootPath(userId);
        var absolutePath = MinioUtils.getAbsolutePath(userRootDirectory, relativePath);

        minioRepository.putEmptyDirectory(minioClientProperties.getBucketName(), absolutePath);
    }
}
