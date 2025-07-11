package com.projects.filestorage.security.session;

import com.projects.filestorage.config.SessionProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionManager {

    private final SessionProperties sessionProperties;

    public void applySessionTimeout(HttpSession session) {
        session.setMaxInactiveInterval(sessionProperties.getTimeout());
    }

    public void expireSessionCookie(HttpServletResponse response) {
        var cookie = new Cookie(sessionProperties.getCookie().getName(), null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(sessionProperties.getCookie().isHttpOnly());
        cookie.setSecure(sessionProperties.getCookie().isSecure());
        response.addCookie(cookie);
    }

    public void invalidateSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
