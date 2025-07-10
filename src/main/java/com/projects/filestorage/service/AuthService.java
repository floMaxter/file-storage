package com.projects.filestorage.service;

import com.projects.filestorage.config.SessionProperties;
import com.projects.filestorage.exception.UnauthenticatedAccessException;
import com.projects.filestorage.exception.UserAlreadyExistsException;
import com.projects.filestorage.repository.UserRepository;
import com.projects.filestorage.web.dto.request.SignInRequestDto;
import com.projects.filestorage.web.dto.request.SignUpRequestDto;
import com.projects.filestorage.web.dto.response.SignInResponseDto;
import com.projects.filestorage.web.dto.response.SignUpResponseDto;
import com.projects.filestorage.web.mapper.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SessionProperties sessionProperties;

    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        if (userRepository.findByUsername(signUpRequestDto.username()).isPresent()) {
            throw new UserAlreadyExistsException("User with username %s already exists".formatted(signUpRequestDto.username()));
        }

        var user = userMapper.toEntity(signUpRequestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.addRole(roleService.getDefaultUserRole());
        userRepository.save(user);

        var signInRequestDto = new SignInRequestDto(signUpRequestDto.username(), signUpRequestDto.password());
        signIn(signInRequestDto, request, response);

        return userMapper.toSignInResponseDto(user);
    }

    public SignInResponseDto signIn(SignInRequestDto signInRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        var authToken = UsernamePasswordAuthenticationToken.unauthenticated(
                signInRequestDto.username(),
                signInRequestDto.password()
        );
        var authResult = authenticationManager.authenticate(authToken);
        var securityContext = securityContextHolderStrategy.createEmptyContext();
        securityContext.setAuthentication(authResult);
        securityContextHolderStrategy.setContext(securityContext);

        securityContextRepository.saveContext(securityContext, request, response);
        request.getSession(true).setMaxInactiveInterval(sessionProperties.getTimeout());

        return new SignInResponseDto(authResult.getName());
    }

    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        var authenticated = securityContextHolderStrategy.getContext().getAuthentication();
        if (authenticated == null || !authenticated.isAuthenticated() || authenticated instanceof AnonymousAuthenticationToken) {
            throw new UnauthenticatedAccessException("User must be authenticated to sign out.");
        }

        securityContextHolderStrategy.clearContext();
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        var cookie = new Cookie(sessionProperties.getCookie().getName(), null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(sessionProperties.getCookie().isHttpOnly());
        cookie.setSecure(sessionProperties.getCookie().isSecure());
        response.addCookie(cookie);
    }
}
