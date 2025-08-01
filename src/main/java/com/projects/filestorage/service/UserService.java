package com.projects.filestorage.service;

import com.projects.filestorage.domain.User;
import com.projects.filestorage.exception.UserAlreadyExistsException;
import com.projects.filestorage.exception.UserNotFoundException;
import com.projects.filestorage.repository.UserRepository;
import com.projects.filestorage.security.context.SecurityContextManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleService roleService;
    private final SecurityContextManager securityContextManager;

    @Transactional(readOnly = true)
    public User getCurrentUserOrThrow() {
        var username = securityContextManager.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with username %s not found", username)));
    }

    @Transactional(readOnly = true)
    public Long getCurrentUserIdOrThrow() {
        return getCurrentUserOrThrow().getId();
    }

    @Transactional
    public User createUser(String username, String password) {
        validateUsernameUniqueness(username);

        var user = new User(username, password);
        user.addRole(roleService.getDefaultUserRole());
        return userRepository.save(user);
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User with username %s already exists", username));
        }
    }
}
