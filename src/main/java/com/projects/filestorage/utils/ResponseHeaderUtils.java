package com.projects.filestorage.utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@UtilityClass
public class ResponseHeaderUtils {

    public void setFileDownloadHeader(HttpServletResponse response, String path) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + MinioUtils.extractResourceName(path) + "\"");
    }

    public void setZipDownloadHeader(HttpServletResponse response, String path) {
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + MinioUtils.extractResourceName(path) + ".zip\"");
    }
}
