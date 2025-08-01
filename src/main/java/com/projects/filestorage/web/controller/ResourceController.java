package com.projects.filestorage.web.controller;

import com.projects.filestorage.service.UserFileService;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {

    private final UserFileService userFileService;

    @GetMapping("/resource")
    @ResponseStatus(HttpStatus.OK)
    public ResourceInfoResponseDto getResourceInfo(@RequestParam("path") String path) {
        return userFileService.getResourceInfo(path);
    }

    @DeleteMapping("/resource")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@RequestParam("path") String path) {
        userFileService.deleteResource(path);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam("path") String path) {
        var resourceDownloadDto = userFileService.downloadResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceDownloadDto.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resourceDownloadDto.responseBody());
    }

    @GetMapping("/resource/move")
    @ResponseStatus(HttpStatus.OK)
    public ResourceInfoResponseDto moveResource(@RequestParam("from") String sourcePath,
                                             @RequestParam("to") String destinationPath) {
        return userFileService.moveResource(sourcePath, destinationPath);
    }

    @GetMapping("/resource/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ResourceInfoResponseDto> searchResources(@RequestParam("query") String query) {
        return userFileService.searchResources(query);
    }

    @PostMapping(value = "/resource")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceInfoResponseDto> uploadResources(@RequestParam("path") String path,
                                                      @RequestParam("files") List<MultipartFile> files) {
        return userFileService.uploadResources(path, files);
    }
}
