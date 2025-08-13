package com.projects.filestorage.integration.service;

import com.projects.filestorage.config.properties.SessionCookieProperties;
import com.projects.filestorage.domain.User;
import com.projects.filestorage.exception.UnauthenticatedAccessException;
import com.projects.filestorage.exception.UserAlreadyExistsException;
import com.projects.filestorage.repository.UserRepository;
import com.projects.filestorage.service.AuthService;
import com.projects.filestorage.service.UserRoleService;
import com.projects.filestorage.service.UserService;
import com.projects.filestorage.web.dto.request.SignInRequestDto;
import com.projects.filestorage.web.dto.request.SignUpRequestDto;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestConfig.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class AuthServiceIT {

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final SessionCookieProperties sessionCookieProperties;

    @ParameterizedTest(name = "Sign-up for {0}")
    @MethodSource("com.projects.filestorage.testdata.data.AuthTestData#getValidSignUpRequestDtos")
    @Transactional
    @Rollback
    void signUp_ValidSignUpRequest_ShouldCreateNewUserAndCreateSession(SignUpRequestDto signUpRequestDto) {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        // when
        var signUpResponseDto = authService.signUp(signUpRequestDto, request, response);

        // then
        assertThat(userRepository.findByUsername(signUpRequestDto.username())).isPresent();
        assertThat(signUpResponseDto.username()).isEqualTo(signUpRequestDto.username());
        assertThat(request.getSession(false)).isNotNull();
    }

    @ParameterizedTest(name = "Sign-up for {0}")
    @MethodSource("com.projects.filestorage.testdata.data.AuthTestData#getInvalidSignUpRequestDtos")
    @Transactional
    @Rollback
    void signUp_InvalidSignUpRequest_ShouldThrownConstraintViolationException(SignUpRequestDto signUpRequestDto) {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        // when

        // then
        assertThatThrownBy(() -> authService.signUp(signUpRequestDto, request, response))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    @Transactional
    @Rollback
    void signUp_NonUniqueUsername_ShouldThrownUserAlreadyExistsException() {
        // given
        var username = "Test user";
        var password = "password";
        var user = new User(null, username, password, Set.of(userRoleService.getDefaultUserRole()));
        userRepository.save(user);

        var signUpRequestDto = new SignUpRequestDto(username, password);
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        // when

        // then
        assertThatThrownBy(() -> authService.signUp(signUpRequestDto, request, response))
                .isInstanceOf(UserAlreadyExistsException.class);
        assertThat(request.getSession(false)).isNull();
    }

    @ParameterizedTest(name = "Sign-in for {0}")
    @MethodSource("com.projects.filestorage.testdata.data.AuthTestData#getValidSignInRequestDtos")
    @Transactional
    @Rollback
    void signIn_ValidSignInRequest_ShouldCreateSession(SignInRequestDto signInRequestDto) {
        // given
        userService.createUser(signInRequestDto.username(), passwordEncoder.encode(signInRequestDto.password()));
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        // when
        var signInResponseDto = authService.signIn(signInRequestDto, request, response);

        // then
        assertThat(userRepository.findByUsername(signInRequestDto.username())).isPresent();
        assertThat(signInRequestDto.username()).isEqualTo(signInResponseDto.username());
        assertThat(request.getSession(false)).isNotNull();

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(signInRequestDto.username());
    }

    @ParameterizedTest(name = "Sign-in for {0}")
    @MethodSource("com.projects.filestorage.testdata.data.AuthTestData#getValidSignInRequestDtos")
    @Transactional
    @Rollback
    void signIn_InvalidPassword_ShouldThrownBadCredentialsException(SignInRequestDto signInRequestDto) {
        // given
        var validPassword = "Correct password";
        var signUpRequestDto = new SignUpRequestDto(signInRequestDto.username(), validPassword);
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        // when
        authService.signUp(signUpRequestDto, request, response);

        // then
        assertThatThrownBy(() -> authService.signIn(signInRequestDto, request, response))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @Transactional
    @Rollback
    void signOut_ValidSignOutRequest_ShouldDeleteSession() {
        // given
        var username = "Username";
        var password = "password";
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var signUpRequestDto = new SignUpRequestDto(username, password);

        // when
        authService.signUp(signUpRequestDto, request, response);
        authService.signOut(request, response);

        // then
        assertThat(request.getSession(false)).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        var cookies = response.getCookies();
        assertThat(Arrays.stream(cookies)
                .anyMatch(c -> c.getName().equals(sessionCookieProperties.getName())
                        && c.getMaxAge() == 0)).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void signOut_SignOutRequestWithoutSession_ShouldThrownUnauthenticatedAccessException() {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        // when

        // then
        assertThatThrownBy(() -> authService.signOut(request, response))
                .isInstanceOf(UnauthenticatedAccessException.class);
    }
}
