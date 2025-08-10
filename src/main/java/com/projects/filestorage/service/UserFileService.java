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

    private final UserService userService;
    private final MinioRepository minioRepository;
    private final MinioResourceDispatcher minioResourceDispatcher;
    private final MinioClientProperties minioClientProperties;
    private final ResourceBusinessValidator resourceValidator;

    public ResourceInfoResponseDto getResourceInfo(String relativePath) {
        var resourceContextDto = buildResourceContextDto(relativePath);
        return minioResourceDispatcher.getResourceInfo(resourceContextDto);
    }

    public List<ResourceInfoResponseDto> getDirectoryInfo(String relativePath) {
        var resourceLocationDto = buildResourceLocationDto(relativePath);

        resourceValidator.validateDirectoryExists(resourceLocationDto.bucket(), resourceLocationDto.absolutePath());

        var objectPaths = minioRepository.listDirectObjectPaths(
                resourceLocationDto.bucket(), resourceLocationDto.absolutePath()
        );

        return objectPaths.stream()
                .map(p -> MinioUtils.getRelativePath(p, resourceLocationDto.rootDirectory()))
                .map(this::getResourceInfo)
                .toList();
    }

    public List<ResourceInfoResponseDto> searchResources(String relativeQuery) {
        var resourceLocationDto = buildResourceLocationDto(relativeQuery);

        var objectPaths = minioRepository.listRecursiveObjectPaths(
                resourceLocationDto.bucket(), resourceLocationDto.absolutePath()
        );

        return objectPaths.stream()
                .map(p -> MinioUtils.getRelativePath(p, resourceLocationDto.rootDirectory()))
                .filter(relPath -> MinioUtils.fileNameMatchesQuery(relPath, relativeQuery))
                .map(this::getResourceInfo)
                .toList();
    }

    public List<ResourceInfoResponseDto> createEmptyDirectory(String relativePath) {
        var resourceLocationDto = buildResourceLocationDto(relativePath);

        resourceValidator.validateDirectoryCreationPreconditions(
                resourceLocationDto.bucket(), resourceLocationDto.absolutePath()
        );

        minioRepository.putEmptyDirectory(resourceLocationDto.bucket(), resourceLocationDto.absolutePath());

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
        var resourceContextDto = buildResourceContextDto(relativePath);
        return minioResourceDispatcher.downloadResource(resourceContextDto);
    }

    public ResourceInfoResponseDto uploadResource(String relativeDirPath, MultipartFile object) {
        var directoryLocationDto = buildResourceLocationDto(relativeDirPath);
        var filePath = directoryLocationDto.absolutePath() + object.getOriginalFilename();

        resourceValidator.validateFileDoesNotExits(directoryLocationDto.bucket(), filePath);

        minioRepository.uploadResource(minioClientProperties.getBucketName(), filePath, object);

        var relativePathToUploadedFile = MinioUtils.getRelativePath(filePath, directoryLocationDto.rootDirectory());

        return getResourceInfo(relativePathToUploadedFile);
    }

    public List<ResourceInfoResponseDto> uploadResources(String relativePath, List<MultipartFile> objects) {
        return objects.stream()
                .map(object -> uploadResource(relativePath, object))
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
        var absolutePath = MinioUtils.getAbsolutePath(relativePath, userRootDirectory);
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
        var absoluteDestinationPath = MinioUtils.getAbsolutePath(userRootDirectory, relativeDestinationPath);

        return new CopyResourceDto(sourceContextDto, absoluteDestinationPath);
    }

    private ResourceLocationDto buildResourceLocationDto(String relativePath) {
        var userRootDirectory = getUserRootDirectory();
        var bucket = minioClientProperties.getBucketName();
        var absolutePath = MinioUtils.getAbsolutePath(relativePath, userRootDirectory);

        return new ResourceLocationDto(bucket, userRootDirectory, absolutePath);
    }

    private String getUserRootDirectory() {
        var currentUserId = userService.getCurrentUserIdOrElseThrow();
        return MinioUtils.buildUserRootPath(currentUserId);
    }
}
