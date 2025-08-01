package com.projects.filestorage.service;

import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.internal.ResourceDownloadDto;
import com.projects.filestorage.web.dto.internal.MinioResourceInfoDto;
import com.projects.filestorage.web.dto.internal.UserPathContextDto;
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

    private final MinioClientService minioClientService;
    private final UserService userService;

    public ResourceInfoResponseDto getResourceInfo(String relativePath) {
        var userPathContextDto = buildUserPathContextDto(relativePath);
        var resultResourceInfo = minioClientService.getResourceInfo(userPathContextDto.absolutePath());
        return mapToResponseDto(resultResourceInfo, userPathContextDto.userRootDirectory());
    }

    public List<ResourceInfoResponseDto> getDirectoryInfo(String relativePath) {
        var userPathContextDto = buildUserPathContextDto(relativePath);
        var directoryResourceInfos = minioClientService.getDirectoryInfo(userPathContextDto.absolutePath());

        return mapToResponseDtos(directoryResourceInfos, userPathContextDto.userRootDirectory());
    }

    public List<ResourceInfoResponseDto> searchResources(String relativePath) {
        var userPathContextDto = buildUserPathContextDto(relativePath);
        var foundResources = minioClientService.searchResources(userPathContextDto.absolutePath());
        
        return mapToResponseDtos(foundResources, userPathContextDto.userRootDirectory());
    }

    public void createUserRootDirectory(Long userId) {
        var userRootPath = MinioUtils.getUserRootDirectory(userId);
        minioClientService.createEmptyDirectory(userRootPath);
    }

    public List<ResourceInfoResponseDto> createEmptyDirectory(String relativePath) {
        var userPathContextDto = buildUserPathContextDto(relativePath);
        var emptyDirectoryResourceInfos = minioClientService.createEmptyDirectory(userPathContextDto.absolutePath());

        return mapToResponseDtos(emptyDirectoryResourceInfos, userPathContextDto.userRootDirectory());
    }

    public ResourceInfoResponseDto moveResource(String relativeSourcePath, String relativeDestinationPath) {
        var sourceUserPathContextDto = buildUserPathContextDto(relativeSourcePath);
        var destinationUserPathContextDto = buildUserPathContextDto(relativeDestinationPath);

        var movedResourceInfoDto = minioClientService.moveResource(sourceUserPathContextDto.absolutePath(),
                destinationUserPathContextDto.absolutePath());

        return mapToResponseDto(movedResourceInfoDto, destinationUserPathContextDto.userRootDirectory());
    }

    public ResourceDownloadDto downloadResource(String relativePath) {
        var userPathContextDto = buildUserPathContextDto(relativePath);
        return minioClientService.downloadResource(userPathContextDto.absolutePath());
    }

    public List<ResourceInfoResponseDto> uploadResources(String relativePath, List<MultipartFile> files) {
        var userPathContextDto = buildUserPathContextDto(relativePath);
        var uploadedResourceInfos = minioClientService.uploadResources(userPathContextDto.absolutePath(), files);
        return mapToResponseDtos(uploadedResourceInfos, userPathContextDto.userRootDirectory());
    }

    public void deleteResource(String relativePath) {
        var userPathContextDto = buildUserPathContextDto(relativePath);
        minioClientService.deleteResource(userPathContextDto.absolutePath());
    }

    private UserPathContextDto buildUserPathContextDto(String relativePath) {
        var currentUserId = userService.getCurrentUserIdOrThrow();
        var userRootDirectory = MinioUtils.getUserRootDirectory(currentUserId);
        return UserPathContextDto.builder()
                .userRootDirectory(userRootDirectory)
                .absolutePath(userRootDirectory + relativePath)
                .build();
    }

    private ResourceInfoResponseDto mapToResponseDto(MinioResourceInfoDto minioResourceInfoDto,
                                                     String userRootDirectory) {
        return ResourceInfoResponseDto.builder()
                .path(stripUserRoot(minioResourceInfoDto.path(), userRootDirectory))
                .name(minioResourceInfoDto.name())
                .size(minioResourceInfoDto.size())
                .resourceType(minioResourceInfoDto.resourceType())
                .build();
    }

    private List<ResourceInfoResponseDto> mapToResponseDtos(List<MinioResourceInfoDto> minioResourceInfoDtos,
                                                            String userRootDirectory) {
        return minioResourceInfoDtos.stream()
                .map(dto -> mapToResponseDto(dto, userRootDirectory))
                .toList();
    }


    private String stripUserRoot(String absolutePath, String userRootDirectory) {
        return absolutePath.startsWith(userRootDirectory)
                ? absolutePath.substring(userRootDirectory.length())
                : absolutePath;
    }
}
