package com.projects.filestorage.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@ConfigurationProperties(prefix = "spring.servlet.multipart")
@Data
public class FileUploadProperties {

    private DataSize maxFileSize = DataSize.ofMegabytes(5);
    private DataSize maxRequestSize = DataSize.ofMegabytes(15);
}
