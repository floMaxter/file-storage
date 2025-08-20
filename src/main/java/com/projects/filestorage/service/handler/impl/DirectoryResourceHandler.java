package com.projects.filestorage.service.handler.impl;

import com.projects.filestorage.exception.MinioAccessException;
import com.projects.filestorage.repository.MinioRepository;
import com.projects.filestorage.service.handler.MinioResourceHandler;
import com.projects.filestorage.service.validator.ResourceBusinessValidator;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.internal.CopyResourceDto;
import com.projects.filestorage.web.dto.internal.ResourceContextDto;
import com.projects.filestorage.web.dto.internal.ResourceDownloadDto;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import com.projects.filestorage.web.mapper.ResourceInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectoryResourceHandler implements MinioResourceHandler {

    private final MinioRepository minioRepository;
    private final ResourceInfoMapper resourceInfoMapper;
    private final ResourceBusinessValidator resourceValidator;

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.DIRECTORY;
    }

    @Override
    public ResourceInfoResponseDto getResourceInfo(ResourceContextDto resourceContextDto) {
        resourceValidator.validateDirectoryExists(resourceContextDto.bucket(), resourceContextDto.absolutePath());

        var relativeParentPath = MinioUtils.extractParentPath(resourceContextDto.relativePath());
        var resourceName = MinioUtils.extractResourceName(resourceContextDto.relativePath());
        var resourceType = resourceContextDto.resourceType();
        var size = 0L;

        return resourceInfoMapper.toResourceInfo(relativeParentPath, resourceName, size, resourceType);
    }

    @Override
    public void copyResource(CopyResourceDto copyResourceDto) {
        var sourceContext = copyResourceDto.sourceContext();

        resourceValidator.validateDirectoryCopyPreconditions(
                sourceContext.bucket(), sourceContext.absolutePath(), copyResourceDto.absoluteDestinationPath()
        );

        var sourceObjectPaths = minioRepository.listRecursiveObjectPaths(
                sourceContext.bucket(),
                sourceContext.absolutePath()
        );

        var destinationObjectPaths = sourceObjectPaths.stream()
                .map(absoluteObjectPath -> absoluteObjectPath.substring(sourceContext.absolutePath().length()))
                .map(relativeObjectPath -> copyResourceDto.absoluteDestinationPath() + relativeObjectPath)
                .toList();

        for (int i = 0; i < sourceObjectPaths.size(); i++) {
            var sourcePath = sourceObjectPaths.get(i);
            var destinationPath = destinationObjectPaths.get(i);
            minioRepository.copyResource(sourceContext.bucket(), sourcePath, destinationPath);
        }
    }

    @Override
    public void deleteResource(ResourceContextDto resourceContextDto) {
        resourceValidator.validateDirectoryExists(resourceContextDto.bucket(), resourceContextDto.absolutePath());
        minioRepository.deleteResources(resourceContextDto.bucket(), resourceContextDto.absolutePath());
    }

    @Override
    public ResourceDownloadDto downloadResource(ResourceContextDto resourceContextDto) {
        resourceValidator.validateDirectoryExists(resourceContextDto.bucket(), resourceContextDto.absolutePath());

        var objectPaths = minioRepository.listRecursiveObjectPaths(
                resourceContextDto.bucket(),
                resourceContextDto.absolutePath()
        );

        var downloadedDirectoryName = Paths.get(resourceContextDto.absolutePath()).getFileName().toString() + ".zip";
        StreamingResponseBody downloadedBody = outputStream -> createZipArchive(
                resourceContextDto,
                objectPaths,
                outputStream
        );

        return ResourceDownloadDto.builder()
                .fileName(downloadedDirectoryName)
                .responseBody(downloadedBody)
                .build();
    }

    private void createZipArchive(ResourceContextDto resourceContextDto, List<String> objectPaths, OutputStream outputStream) {
        var absolutePathToDirectory = resourceContextDto.absolutePath();

        try (var zipOutputStream = new ZipOutputStream(outputStream)) {
            for (var absoluteObjectPath : objectPaths) {
                var relativeObjectPath = absoluteObjectPath.substring(absolutePathToDirectory.length());

                var zipEntry = new ZipEntry(relativeObjectPath);
                zipEntry.setTime(System.currentTimeMillis());
                zipOutputStream.putNextEntry(zipEntry);

                var objectResponse = minioRepository.getObject(resourceContextDto.bucket(), absoluteObjectPath);

                StreamUtils.copy(objectResponse, zipOutputStream);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error when download a directory on the path '{}'", absolutePathToDirectory, ex);
            throw new MinioAccessException(String.format(
                    "Unexpected error when download a directory on the path '%s'", resourceContextDto.absolutePath()));
        }
    }
}