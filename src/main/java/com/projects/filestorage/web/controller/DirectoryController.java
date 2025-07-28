package com.projects.filestorage.web.controller;

import com.projects.filestorage.service.MinioClientService;
import com.projects.filestorage.web.dto.response.ResourceInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final MinioClientService minioClientService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ResourceInfoDto> getDirectoryInfo(@RequestParam("path") String path) {
        return minioClientService.getDirectoryInfo(path);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceInfoDto> createEmptyDirectory(@RequestParam("path") String path) {
        return minioClientService.createEmptyDirectory(path);
    }
}
