package com.projects.filestorage.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "server.servlet.session.cookie")
@Data
public class SessionCookieProperties {

    private String name;
    private boolean httpOnly;
    private boolean secure;
}
