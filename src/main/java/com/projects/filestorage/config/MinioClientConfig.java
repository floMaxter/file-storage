package com.projects.filestorage.config;

import com.projects.filestorage.config.properties.MinioClientProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioClientConfig {

    private final MinioClientProperties minioClientProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioClientProperties.getEndpoint())
                .credentials(minioClientProperties.getAccessKey(), minioClientProperties.getSecretKey())
                .build();
    }
}
