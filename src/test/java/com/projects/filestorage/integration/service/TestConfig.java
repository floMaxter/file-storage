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

    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(Redis.REDIS_IMAGE).withExposedPorts(Redis.REDIS_PORT);
    }

    public static final GenericContainer<?> minio = new GenericContainer<>(Minio.MINIO_IMAGE)
            .withEnv(Minio.ENV_MINIO_ROOT_USER, Minio.MINIO_ACCESS_KEY)
            .withEnv(Minio.ENV_MINIO_ROOT_PASSWORD, Minio.MINIO_SECRET_KEY)
            .withCommand(Minio.MINIO_START_COMMANDS)
            .withExposedPorts(Minio.ENV_MINIO_PORT);

    static {
        minio.start();
    }

    public static class Minio {
        public static final DockerImageName MINIO_IMAGE = DockerImageName.parse("minio/minio:latest");

        public static final String ENV_MINIO_ROOT_USER = "MINIO_ACCESS_KEY";
        public static final String ENV_MINIO_ROOT_PASSWORD = "MINIO_SECRET_KEY";
        public static final int ENV_MINIO_PORT = 9000;

        public static final String PROP_MINIO_ENDPOINT = "minio.endpoint";
        public static final String PROP_MINIO_ACCESS_KEY = "minio.access-key";
        public static final String PROP_MINIO_SECRET_KEY = "minio.secret-key";
        public static final String PROP_MINIO_BUCKET_NAME = "minio.bucket-name";

        public static final String MINIO_ACCESS_KEY = "test_admin";
        public static final String MINIO_SECRET_KEY = "test_admin_password";
        public static final String MINIO_BUCKET_NAME = "test-user-files";
        public static final String MINIO_HTTP_PROTOCOL = "http://";

        public static final String MINIO_START_COMMANDS = "server /data";

        public static final String MINI0_TEST_USERNAME = "test_username";
        public static final String MINIO_TEST_PASSWORD = "test_password";
    }

    public static class Postgres {
        public static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16.0");
    }

    public static class Redis {
        public static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.0");
        public static final int REDIS_PORT = 6379;
    }
}
