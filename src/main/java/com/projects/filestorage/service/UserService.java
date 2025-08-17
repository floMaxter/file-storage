package com.projects.filestorage.service;

import com.projects.filestorage.domain.User;
import com.projects.filestorage.exception.UserAlreadyExistsException;
import com.projects.filestorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleService roleService;

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
