package com.projects.filestorage.web.controller;

import com.projects.filestorage.service.MinioClientService;
import com.projects.filestorage.web.dto.response.ResourceInfoDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {

    private final MinioClientService minioClientService;

    @GetMapping("/resource")
    @ResponseStatus(HttpStatus.OK)
    public ResourceInfoDto getResourceInfo(@RequestParam("path") String path) {
        return minioClientService.getResourceInfo(path);
    }

    @DeleteMapping("/resource")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@RequestParam("path") String path) {
        minioClientService.deleteResource(path);
    }

    @GetMapping("/resource/download")
    @ResponseStatus(HttpStatus.OK)
    public void downloadResource(@RequestParam("path") String path,
                                 HttpServletResponse response) {
        minioClientService.downloadResource(response, path);
    }

    @GetMapping("/resource/move")
    @ResponseStatus(HttpStatus.OK)
    public ResourceInfoDto moveResource(@RequestParam("from") String sourcePath,
                                        @RequestParam("to") String destinationPath) {
        return minioClientService.moveResource(sourcePath, destinationPath);
    }

    @GetMapping("/resource/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ResourceInfoDto> searchResources(@RequestParam("query") String query) {
        return minioClientService.searchResources(query);
    }

    @PostMapping(value = "/resource")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceInfoDto> uploadResources(@RequestParam("path") String path,
                                                 @RequestParam("files") List<MultipartFile> files) {
        return minioClientService.uploadResources(path, files);
    }
}
