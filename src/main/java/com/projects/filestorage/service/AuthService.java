package com.projects.filestorage.service;

import com.projects.filestorage.exception.UnauthenticatedAccessException;
import com.projects.filestorage.security.context.SecurityContextManager;
import com.projects.filestorage.security.session.SessionManager;
import com.projects.filestorage.web.dto.request.SignInRequestDto;
import com.projects.filestorage.web.dto.request.SignUpRequestDto;
import com.projects.filestorage.web.dto.response.SignInResponseDto;
import com.projects.filestorage.web.dto.response.SignUpResponseDto;
import com.projects.filestorage.web.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserFileService userFileService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SessionManager sessionManager;
    private final SecurityContextManager securityContextManager;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        log.info("[Start] Sign-up for username={}", signUpRequestDto.username());

        var user = userService.createUser(signUpRequestDto.username(),
                passwordEncoder.encode(signUpRequestDto.password()));

        userFileService.createUserRootDir(user.getId());

        authenticateAndStartSession(signUpRequestDto.username(), signUpRequestDto.password(), request, response);

        log.info("[Success] Signed up for username={}", signUpRequestDto.username());
        return userMapper.toSignInResponseDto(user);
    }

    @Transactional(readOnly = true)
    public SignInResponseDto signIn(SignInRequestDto signInRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        log.info("[Start] Sign-in for username={}", signInRequestDto.username());

        authenticateAndStartSession(signInRequestDto.username(), signInRequestDto.password(), request, response);

        log.info("[Success] Signed in for username={}", signInRequestDto.username());
        return new SignInResponseDto(signInRequestDto.username());
    }

    private void authenticateAndStartSession(String username,
                                             String password,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        log.info("[Start] Authenticate and start session for username={}", username);

        var authResult = securityContextManager.authenticate(username, password);
        securityContextManager.setupSecurityContext(authResult, request, response);
        sessionManager.applySessionTimeout(request.getSession(true));

        log.info("[Success] Authenticated user and created HTTP session for username={}", username);
    }

    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        log.info("[Start] Sign-out request received");

        validateAuthentication();

        var username = securityContextManager.getCurrentUsername();
        securityContextManager.clearContext();
        sessionManager.invalidateSession(request);
        sessionManager.expireSessionCookie(response);

        log.info("[Success] User signed out: {}", username);
    }

    private void validateAuthentication() {
        if (!securityContextManager.isAuthenticated()) {
            log.warn("User must be authenticated to sign out");
            throw new UnauthenticatedAccessException("User must be authenticated to sign out");
        }
    }
}
