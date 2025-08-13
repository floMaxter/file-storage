package com.projects.filestorage.security.session;

import com.projects.filestorage.config.properties.SessionCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionManager {

    private final SessionCookieProperties sessionCookieProperties;

    public void expireSessionCookie(HttpServletResponse response) {
        var cookie = new Cookie(sessionCookieProperties.getName(), null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(sessionCookieProperties.isHttpOnly());
        cookie.setSecure(sessionCookieProperties.isSecure());
        response.addCookie(cookie);
    }

    public void invalidateSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
