package com.projects.filestorage.integration.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(Postgres.POSTGRES_IMAGE);
    }

    public static final GenericContainer<?> minio = new GenericContainer<>(Minio.MINIO_IMAGE)
            .withEnv("MINIO_ACCESS_KEY", "test_admin")
            .withEnv("MINIO_SECRET_KEY", "test_admin_password")
            .withCommand("server /data")
            .withExposedPorts(9000);

    static {
        minio.start();
    }

    public static class Minio {
        public static final DockerImageName MINIO_IMAGE = DockerImageName.parse("minio/minio:latest");

        public static final String PROP_MINIO_ENDPOINT = "minio.endpoint";
        public static final String PROP_MINIO_ACCESS_KEY = "minio.access-key";
        public static final String PROP_MINIO_SECRET_KEY = "minio.secret-key";
        public static final String PROP_MINIO_BUCKET_NAME = "minio.bucket-name";

        public static final String MINIO_ACCESS_KEY = "test_admin";
        public static final String MINIO_SECRET_KEY = "test_admin_password";
        public static final String MINIO_BUCKET_NAME = "test-user-files";
        public static final String MINIO_HTTP_PROTOCOL = "http://";

        public static final String MINI0_TEST_USERNAME = "test_username";
        public static final String MINIO_TEST_PASSWORD = "test_password";
    }

    public static class Postgres {
        public static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16.0");
    }
}
