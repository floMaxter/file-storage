package com.projects.filestorage.web.controller;

import com.projects.filestorage.service.MinioClientService;
import com.projects.filestorage.web.dto.response.ResourceInfoDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/storage/")
@RequiredArgsConstructor
public class FileStorageController {

    private final MinioClientService minioClientService;

    @GetMapping("/resource")
    public ResourceInfoDto getResourceInfo(@RequestParam("path") String path) {
        return minioClientService.getResourceInfo(path);
    }

    @DeleteMapping("/resource")
    public void deleteResource(@RequestParam("path") String path) {
        minioClientService.deleteResource(path);
    }

    @PostMapping(value = "/resource")
    public List<ResourceInfoDto> uploadResources(@RequestParam("path") String path,
                                                 @RequestParam("files") List<MultipartFile> files) {
        return minioClientService.uploadResources(path, files);
    }

    @GetMapping("/resource/search")
    public List<ResourceInfoDto> searchResources(@RequestParam("query") String query) {
        return minioClientService.searchResources(query);
    }

    @GetMapping("/resource/download")
    public void downloadResource(@RequestParam("path") String path,
                                 HttpServletResponse response) {
        minioClientService.downloadResource(response, path);
    }

    @GetMapping("/resource/move")
    public ResourceInfoDto moveResource(@RequestParam("from") String sourcePath,
                                        @RequestParam("to") String destinationPath) {
        return minioClientService.moveResource(sourcePath, destinationPath);
    }

    @GetMapping("/directory")
    public List<ResourceInfoDto> getDirectoryInfo(@RequestParam("path") String path) {
        return minioClientService.getDirectoryInfo(path);
    }

    @PostMapping("/directory")
    public List<ResourceInfoDto> createEmptyDirectory(@RequestParam("path") String path) {
        return minioClientService.createEmptyDirectory(path);
    }
}
