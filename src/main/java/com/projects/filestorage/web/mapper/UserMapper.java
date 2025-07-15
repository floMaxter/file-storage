package com.projects.filestorage.web.mapper;

import com.projects.filestorage.domain.User;
import com.projects.filestorage.web.dto.request.SignUpRequestDto;
import com.projects.filestorage.web.dto.response.SignUpResponseDto;
import com.projects.filestorage.web.dto.response.UserDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class UserMapper {

    public User toEntity(SignUpRequestDto signUpRequestDto) {
        return User.builder()
                .username(signUpRequestDto.username())
                .password(signUpRequestDto.password())
                .roles(new HashSet<>())
                .build();
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getUsername());
    }

    public SignUpResponseDto toSignInResponseDto(User user) {
        return new SignUpResponseDto(user.getUsername());
    }
}
