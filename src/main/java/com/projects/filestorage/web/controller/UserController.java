package com.projects.filestorage.web.controller;

import com.projects.filestorage.exception.UserNotFoundException;
import com.projects.filestorage.service.UserService;
import com.projects.filestorage.web.dto.response.UserDto;
import com.projects.filestorage.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getCurrentUser(Authentication authentication) {
        var userDetails = (UserDetails) authentication.getPrincipal();
        var userDto = userService.findUser(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException(String.format("User with username %s not found", userDetails.getUsername())));
        return userMapper.toDto(userDto);
    }
}
