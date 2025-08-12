package com.projects.filestorage.config;

import com.projects.filestorage.service.handler.MinioResourceDispatcher;
import com.projects.filestorage.service.handler.MinioResourceHandler;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class MinioResourceDispatcherConfig {

    @Bean
    public MinioResourceDispatcher minioResourceDispatcher(List<MinioResourceHandler> handlers) {
        Map<ResourceType, MinioResourceHandler> handlerMap = handlers.stream()
                .collect(Collectors.toMap(MinioResourceHandler::getSupportedType, Function.identity()));

        return new MinioResourceDispatcher(handlerMap);
    }
}
