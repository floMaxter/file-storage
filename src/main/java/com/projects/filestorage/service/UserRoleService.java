package com.projects.filestorage.service;

import com.projects.filestorage.domain.Role;
import com.projects.filestorage.exception.UserRoleNotFoundException;
import com.projects.filestorage.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final RoleRepository roleRepository;

    private static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    public Role getDefaultUserRole() {
        return roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseThrow(() -> new UserRoleNotFoundException(String.format("Role %s not found", DEFAULT_ROLE_NAME)));
    }
}
