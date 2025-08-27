package com.projects.filestorage.integration.service;

import com.projects.filestorage.config.properties.MinioClientProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;

import static com.projects.filestorage.integration.service.TestConfig.minio;

@SpringBootTest(classes = TestConfig.class)
@NoArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class AbstractIntegrationTest {

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add(TestConfig.Minio.PROP_MINIO_ENDPOINT,
                () -> TestConfig.Minio.MINIO_HTTP_PROTOCOL + minio.getHost() + ":" + minio.getFirstMappedPort()
        );
        registry.add(TestConfig.Minio.PROP_MINIO_ACCESS_KEY, () -> TestConfig.Minio.MINIO_ACCESS_KEY);
        registry.add(TestConfig.Minio.PROP_MINIO_SECRET_KEY, () -> TestConfig.Minio.MINIO_SECRET_KEY);
        registry.add(TestConfig.Minio.PROP_MINIO_BUCKET_NAME, () -> TestConfig.Minio.MINIO_BUCKET_NAME);
    }

    @BeforeEach
    void setupBucket(@Autowired MinioClientProperties minioClientProperties) throws Exception {
        var minioClient = MinioClient.builder()
                .endpoint(minioClientProperties.getEndpoint())
                .credentials(minioClientProperties.getAccessKey(), minioClientProperties.getSecretKey())
                .build();

        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioClientProperties.getBucketName())
                .build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .build());
        }

        minioClient.close();
    }
}
