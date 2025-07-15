package com.projects.filestorage.testdata.data;

import com.projects.filestorage.web.dto.request.SignInRequestDto;
import com.projects.filestorage.web.dto.request.SignUpRequestDto;

import java.util.stream.Stream;

public class AuthTestData {

    private static final SignUpRequestDto VALID_SIGN_UP_1 = new SignUpRequestDto("userAlpha", "password1");
    private static final SignUpRequestDto VALID_SIGN_UP_2 = new SignUpRequestDto("userBeta", "password2");
    private static final SignUpRequestDto VALID_SIGN_UP_3 = new SignUpRequestDto("userGamma", "password3");

    private static final SignUpRequestDto INVALID_SIGN_UP_1 = new SignUpRequestDto("u", "password1");
    private static final SignUpRequestDto INVALID_SIGN_UP_2 = new SignUpRequestDto("", "password2");
    private static final SignUpRequestDto INVALID_SIGN_UP_3 = new SignUpRequestDto("u", "p");

    private static final SignInRequestDto VALID_SIGN_IN_1 = new SignInRequestDto("userAlpha", "password1");
    private static final SignInRequestDto VALID_SIGN_IN_2 = new SignInRequestDto("userBeta", "password2");
    private static final SignInRequestDto VALID_SIGN_IN_3 = new SignInRequestDto("userGamma", "password3");

    public static Stream<SignUpRequestDto> getValidSignUpRequestDtos() {
        return Stream.of(VALID_SIGN_UP_1, VALID_SIGN_UP_2, VALID_SIGN_UP_3);
    }

    public static Stream<SignUpRequestDto> getInvalidSignUpRequestDtos() {
        return Stream.of(INVALID_SIGN_UP_1, INVALID_SIGN_UP_2, INVALID_SIGN_UP_3);
    }

    public static Stream<SignInRequestDto> getValidSignInRequestDtos() {
        return Stream.of(VALID_SIGN_IN_1, VALID_SIGN_IN_2, VALID_SIGN_IN_3);
    }
}
