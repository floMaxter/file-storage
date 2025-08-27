package com.projects.filestorage.web.mapper;

import com.projects.filestorage.domain.User;
import com.projects.filestorage.security.CustomUserDetails;
import com.projects.filestorage.web.dto.response.SignUpResponseDto;
import com.projects.filestorage.web.dto.response.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(CustomUserDetails userDetails) {
        return new UserDto(userDetails.getUsername());
    }

    public SignUpResponseDto toSignInResponseDto(User user) {
        return new SignUpResponseDto(user.getUsername());
    }
}
