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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SessionManager sessionManager;
    private final SecurityContextManager securityContextManager;

    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        validateUsernameUniqueness(signUpRequestDto.username());

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
        var authResult = securityContextManager.authenticate(signInRequestDto.username(), signInRequestDto.password());
        securityContextManager.setupSecurityContext(authResult, request, response);
        sessionManager.applySessionTimeout(request.getSession(true));

        return new SignInResponseDto(authResult.getName());
    }

    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        validateAuthentication();
        securityContextManager.clearContext();
        sessionManager.invalidateSession(request);
        sessionManager.expireSessionCookie(response);
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User with username %s already exists", username));
        }
    }

    private void validateAuthentication() {
        if (!securityContextManager.isAuthenticated()) {
            throw new UnauthenticatedAccessException("User must be authenticated to sign out");
        }
    }
}
