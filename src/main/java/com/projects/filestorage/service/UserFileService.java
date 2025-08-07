package com.projects.filestorage.service;

import com.projects.filestorage.config.MinioClientProperties;
import com.projects.filestorage.repository.MinioRepository;
import com.projects.filestorage.service.handler.MinioResourceDispatcher;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.internal.CopyResourceDto;
import com.projects.filestorage.web.dto.internal.ResourceContextDto;
import com.projects.filestorage.web.dto.internal.ResourceDownloadDto;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFileService {

    private final UserService userService;
    private final MinioRepository minioRepository;
    private final MinioResourceDispatcher minioResourceDispatcher;
    private final MinioClientProperties minioClientProperties;

    public ResourceInfoResponseDto getResourceInfo(String relativePath) {
        var resourceContextDto = buildResourceContextDto(relativePath);
        return minioResourceDispatcher.getResourceInfo(resourceContextDto);
    }

    public List<ResourceInfoResponseDto> getDirectoryInfo(String relativePath) {
        var userRootDirectory = getUserRootDirectory();
        var absolutePath = MinioUtils.getAbsolutePath(relativePath, userRootDirectory);
        var objectPaths = minioRepository.listDirectObjectPaths(minioClientProperties.getBucketName(), absolutePath);

        return objectPaths.stream()
                .map(p -> MinioUtils.getRelativePath(p, userRootDirectory))
                .map(this::getResourceInfo)
                .toList();
    }

    public List<ResourceInfoResponseDto> searchResources(String relativePath) {
        var userRootDirectory = getUserRootDirectory();
        var absolutePath = MinioUtils.getAbsolutePath(relativePath, userRootDirectory);
        var objectPaths = minioRepository.listRecursiveObjectPaths(minioClientProperties.getBucketName(), absolutePath);

        return objectPaths.stream()
                .map(p -> MinioUtils.getRelativePath(p, userRootDirectory))
                .map(this::getResourceInfo)
                .toList();
    }

    public List<ResourceInfoResponseDto> createEmptyDir(String relativePath) {
        var userRootDirectory = getUserRootDirectory();
        var absolutePath = MinioUtils.getAbsolutePath(relativePath, userRootDirectory);
        minioRepository.putEmptyDirectory(minioClientProperties.getBucketName(), absolutePath);

        return getDirectoryInfo(relativePath);
    }

    public void createUserRootDir(Long userId) {
        var userRootPath = MinioUtils.buildUserRootPath(userId);
        minioRepository.putEmptyDirectory(minioClientProperties.getBucketName(), userRootPath);
    }

    public ResourceInfoResponseDto moveResource(String relativeSourcePath, String relativeDestinationPath) {
        var copyResourceDto = buildMoveResourceDto(relativeSourcePath, relativeDestinationPath);

        minioResourceDispatcher.copyResource(copyResourceDto);
        deleteResource(relativeSourcePath);

        return getResourceInfo(relativeDestinationPath);
    }

    public ResourceDownloadDto downloadResource(String relativePath) {
        var userPathContextDto = buildResourceContextDto(relativePath);
        return minioResourceDispatcher.downloadResource(userPathContextDto);
    }

    public ResourceInfoResponseDto uploadResource(String relativeDirPath, MultipartFile file) {
        var userRoot = getUserRootDirectory();
        var dirPath = MinioUtils.getAbsolutePath(relativeDirPath, userRoot);
        var filePath = dirPath + file.getOriginalFilename();

        minioRepository.uploadResource(minioClientProperties.getBucketName(), filePath, file);

        var relativePathToUploadedFile = MinioUtils.getRelativePath(filePath, userRoot);
        return getResourceInfo(relativePathToUploadedFile);
    }

    public List<ResourceInfoResponseDto> uploadResources(String relativePath, List<MultipartFile> files) {
        return files.stream()
                .map(file -> uploadResource(relativePath, file))
                .toList();
    }

    public void deleteResource(String relativePath) {
        var resourceContextDto = buildResourceContextDto(relativePath);
        minioResourceDispatcher.deleteResource(resourceContextDto);
        ensureDirectoryPlaceholder(resourceContextDto);
    }

    private void ensureDirectoryPlaceholder(ResourceContextDto resourceContextDto) {
        var prefix = MinioUtils.extractParentPath(resourceContextDto.absolutePath());
        minioRepository.ensureDirectoryPlaceholder(resourceContextDto.bucket(), prefix);
    }

    private ResourceContextDto buildResourceContextDto(String relativePath) {
        var userRootDirectory = getUserRootDirectory();
        var bucket = minioClientProperties.getBucketName();
        var absolutePath = userRootDirectory + relativePath;
        var resourceType = minioRepository.resolveResourceType(bucket, absolutePath);

        return ResourceContextDto.builder()
                .bucket(bucket)
                .absolutePath(absolutePath)
                .relativePath(relativePath)
                .resourceType(resourceType)
                .build();
    }

    private CopyResourceDto buildMoveResourceDto(String relativeSourcePath, String relativeDestinationPath) {
        var sourceContextDto = buildResourceContextDto(relativeSourcePath);
        var userRootDirectory = getUserRootDirectory();
        var absoluteDestinationPath = userRootDirectory + relativeDestinationPath;

        return new CopyResourceDto(sourceContextDto, absoluteDestinationPath);
    }

    private String getUserRootDirectory() {
        var currentUserId = userService.getCurrentUserIdOrThrow();
        return MinioUtils.buildUserRootPath(currentUserId);
    }
}
