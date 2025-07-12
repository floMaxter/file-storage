package com.projects.filestorage.integration.service;

import com.projects.filestorage.domain.User;
import com.projects.filestorage.exception.UserAlreadyExistsException;
import com.projects.filestorage.repository.UserRepository;
import com.projects.filestorage.service.AuthService;
import com.projects.filestorage.service.UserRoleService;
import com.projects.filestorage.web.dto.request.SignInRequestDto;
import com.projects.filestorage.web.dto.request.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestConfig.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class AuthServiceIT {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;

    @ParameterizedTest(name = "Sign-up for {0}")
    @MethodSource("com.projects.filestorage.testdata.data.AuthTestData#getValidateSignUpRequestDtos")
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
    }

    @ParameterizedTest(name = "Sign-in for {0}")
    @MethodSource("com.projects.filestorage.testdata.data.AuthTestData#getValidateSignInRequestDtos")
    @Transactional
    @Rollback
    void signIn_ValidSignInRequest_ShouldCreateSession(SignInRequestDto signInRequestDto) {

    }
}
