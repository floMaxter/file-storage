package com.projects.filestorage.service;

import com.projects.filestorage.exception.UnauthenticatedAccessException;
import com.projects.filestorage.exception.UserAlreadyExistsException;
import com.projects.filestorage.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserRoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SessionManager sessionManager;
    private final SecurityContextManager securityContextManager;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
//        validateUsernameUniqueness(signUpRequestDto.username());
//
//        var user = userMapper.toEntity(signUpRequestDto);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.addRole(roleService.getDefaultUserRole());
//        userRepository.save(user);
        
        var user = userService.createUser(signUpRequestDto.username(),
                passwordEncoder.encode(signUpRequestDto.password()));
        authenticateAndStartSession(signUpRequestDto.username(), signUpRequestDto.password(), request, response);

        return userMapper.toSignInResponseDto(user);
    }

    @Transactional(readOnly = true)
    public SignInResponseDto signIn(SignInRequestDto signInRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        authenticateAndStartSession(signInRequestDto.username(), signInRequestDto.password(), request, response);
        return new SignInResponseDto(signInRequestDto.username());
    }

    private void authenticateAndStartSession(String username,
                                             String password,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        var authResult = securityContextManager.authenticate(username, password);
        securityContextManager.setupSecurityContext(authResult, request, response);
        sessionManager.applySessionTimeout(request.getSession(true));
    }

    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        validateAuthentication();
        securityContextManager.clearContext();
        sessionManager.invalidateSession(request);
        sessionManager.expireSessionCookie(response);
    }

//    private void validateUsernameUniqueness(String username) {
//        if (userRepository.findByUsername(username).isPresent()) {
//            throw new UserAlreadyExistsException(String.format("User with username %s already exists", username));
//        }
//    }

    private void validateAuthentication() {
        if (!securityContextManager.isAuthenticated()) {
            throw new UnauthenticatedAccessException("User must be authenticated to sign out");
        }
    }
}
