package com.projects.filestorage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI defineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("File storage API")
                        .version("1.0.0")
                        .description("REST API for storage and management files"))
                .addSecurityItem(new SecurityRequirement().addList("sessionAuth"))
                .components(new Components()
                        .addSecuritySchemes("sessionAuth", new SecurityScheme()
                                .name("JSESSIONID")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)));
    }
}
