package com.projects.filestorage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "server.servlet.session")
@Data
public class SessionProperties {

    private Cookie cookie= new Cookie();
    private int timeout;

    @Data
    public static class Cookie {

        private String name;
        private boolean httpOnly;
        private boolean secure;
    }
}
