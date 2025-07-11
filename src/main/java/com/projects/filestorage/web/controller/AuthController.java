package com.projects.filestorage.web.controller;

import com.projects.filestorage.service.AuthService;
import com.projects.filestorage.web.dto.request.SignInRequestDto;
import com.projects.filestorage.web.dto.request.SignUpRequestDto;
import com.projects.filestorage.web.dto.response.SignInResponseDto;
import com.projects.filestorage.web.dto.response.SignUpResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.OK)
    public SignUpResponseDto signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        return authService.signUp(signUpRequestDto, request, response);
    }

    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.CREATED)
    public SignInResponseDto signIn(@Valid @RequestBody SignInRequestDto signInRequestDto,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        return authService.signIn(signInRequestDto, request, response);
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        authService.signOut(request, response);
    }
}
