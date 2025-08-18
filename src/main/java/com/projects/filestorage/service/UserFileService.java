package com.projects.filestorage.service;

import com.projects.filestorage.config.properties.MinioClientProperties;
import com.projects.filestorage.repository.MinioRepository;
import com.projects.filestorage.service.handler.MinioResourceDispatcher;
import com.projects.filestorage.service.validator.ResourceBusinessValidator;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.internal.CopyResourceDto;
import com.projects.filestorage.web.dto.internal.ResourceContextDto;
import com.projects.filestorage.web.dto.internal.ResourceDownloadDto;
import com.projects.filestorage.web.dto.internal.enums.ResourceLocationDto;
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

    private final MinioRepository minioRepository;
    private final MinioResourceDispatcher minioResourceDispatcher;
    private final MinioClientProperties minioClientProperties;
    private final ResourceBusinessValidator resourceValidator;

    public ResourceInfoResponseDto getResourceInfo(Long userId, String relativePath) {
        var resourceContextDto = buildResourceContextDto(userId, relativePath);
        return minioResourceDispatcher.getResourceInfo(resourceContextDto);
    }

    public List<ResourceInfoResponseDto> getDirectoryInfo(Long userId, String relativeDirPath) {
        var resourceLocationDto = buildResourceLocationDto(userId, relativeDirPath);

        resourceValidator.validateDirectoryExists(resourceLocationDto.bucket(), resourceLocationDto.absolutePath());

        var objectPaths = minioRepository.listDirectObjectPaths(
                resourceLocationDto.bucket(), resourceLocationDto.absolutePath()
        );

        return objectPaths.stream()
                .map(absolutePath -> MinioUtils.getRelativePath(resourceLocationDto.rootDirectory(), absolutePath))
                .map(relativePath -> buildResourceContextDto(userId, relativePath))
                .map(minioResourceDispatcher::getResourceInfo)
                .toList();
    }

    public List<ResourceInfoResponseDto> searchResources(Long userId, String relativeQuery) {
        var resourceLocationDto = buildResourceLocationDto(userId, relativeQuery);

        var objectPaths = minioRepository.listRecursiveObjectPaths(
                resourceLocationDto.bucket(), resourceLocationDto.rootDirectory()
        );

        return objectPaths.stream()
                .map(absolutePath -> MinioUtils.getRelativePath(resourceLocationDto.rootDirectory(), absolutePath))
                .filter(relativePath -> MinioUtils.fileNameMatchesQuery(relativePath, relativeQuery))
                .map(relativePath -> buildResourceContextDto(userId, relativePath))
                .map(minioResourceDispatcher::getResourceInfo)
                .toList();
    }

    public List<ResourceInfoResponseDto> createEmptyDirectory(Long userId, String relativePath) {
        var resourceLocationDto = buildResourceLocationDto(userId, relativePath);

        resourceValidator.validateDirectoryCreationPreconditions(
                resourceLocationDto.bucket(), resourceLocationDto.absolutePath()
        );

        minioRepository.putEmptyDirectory(resourceLocationDto.bucket(), resourceLocationDto.absolutePath());

        return getDirectoryInfo(userId, relativePath);
    }

    public void createUserRootDir(Long userId) {
        var userRootPath = MinioUtils.buildUserRootPath(userId);
        minioRepository.putEmptyDirectory(minioClientProperties.getBucketName(), userRootPath);
    }

    public ResourceInfoResponseDto moveResource(Long userId,
                                                String relativeSourcePath,
                                                String relativeDestinationPath) {
        var copyResourceDto = buildMoveResourceDto(userId, relativeSourcePath, relativeDestinationPath);

        minioResourceDispatcher.copyResource(copyResourceDto);
        deleteResource(userId, relativeSourcePath);

        return getResourceInfo(userId, relativeDestinationPath);
    }

    public ResourceDownloadDto downloadResource(Long userId, String relativePath) {
        var resourceContextDto = buildResourceContextDto(userId, relativePath);
        return minioResourceDispatcher.downloadResource(resourceContextDto);
    }

    public ResourceInfoResponseDto uploadResource(Long userId,
                                                  String relativeDirPath,
                                                  MultipartFile object) {
        var directoryLocationDto = buildResourceLocationDto(userId, relativeDirPath);
        var filePath = directoryLocationDto.absolutePath() + object.getOriginalFilename();

        resourceValidator.validateFileDoesNotExits(directoryLocationDto.bucket(), filePath);

        minioRepository.uploadResource(minioClientProperties.getBucketName(), filePath, object);

        var relativePathToUploadedFile = MinioUtils.getRelativePath(directoryLocationDto.rootDirectory(), filePath);

        return getResourceInfo(userId, relativePathToUploadedFile);
    }

    public List<ResourceInfoResponseDto> uploadResources(Long userId,
                                                         String relativePath,
                                                         List<MultipartFile> objects) {
        return objects.stream()
                .map(object -> uploadResource(userId, relativePath, object))
                .toList();
    }

    public void deleteResource(Long userId, String relativePath) {
        var resourceContextDto = buildResourceContextDto(userId, relativePath);
        minioResourceDispatcher.deleteResource(resourceContextDto);
        ensureDirectoryPlaceholder(resourceContextDto);
    }

    private void ensureDirectoryPlaceholder(ResourceContextDto resourceContextDto) {
        var prefix = MinioUtils.extractParentPath(resourceContextDto.absolutePath());
        minioRepository.ensureDirectoryPlaceholder(resourceContextDto.bucket(), prefix);
    }

    private ResourceContextDto buildResourceContextDto(Long userId, String relativePath) {
        var userRootDirectory = MinioUtils.buildUserRootPath(userId);
        var bucket = minioClientProperties.getBucketName();
        var absolutePath = MinioUtils.getAbsolutePath(userRootDirectory, relativePath);
        var resourceType = minioRepository.resolveResourceType(bucket, absolutePath);

        return ResourceContextDto.builder()
                .bucket(bucket)
                .absolutePath(absolutePath)
                .relativePath(relativePath)
                .resourceType(resourceType)
                .build();
    }

    private CopyResourceDto buildMoveResourceDto(Long userId, String relativeSourcePath, String relativeDestinationPath) {
        var sourceContextDto = buildResourceContextDto(userId, relativeSourcePath);
        var userRootDirectory = MinioUtils.buildUserRootPath(userId);
        var absoluteDestinationPath = MinioUtils.getAbsolutePath(userRootDirectory, relativeDestinationPath);

        return new CopyResourceDto(sourceContextDto, absoluteDestinationPath);
    }

    private ResourceLocationDto buildResourceLocationDto(Long userId, String relativePath) {
        var userRootDirectory = MinioUtils.buildUserRootPath(userId);
        var bucket = minioClientProperties.getBucketName();
        var absolutePath = MinioUtils.getAbsolutePath(userRootDirectory, relativePath);

        return new ResourceLocationDto(bucket, userRootDirectory, absolutePath);
    }
}
