package com.example.pim.service;

import com.example.pim.domain.Role;
import com.example.pim.domain.User;
import com.example.pim.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String password, Set<Role> roles) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("User with username '" + username + "' already exists.");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @PostConstruct
    public void initDefaultUsers() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            createUser("admin", "adminpass", Set.of(Role.ADMIN, Role.EDITOR, Role.REVIEWER));
        }
        if (userRepository.findByUsername("editor").isEmpty()) {
            createUser("editor", "editorpass", Set.of(Role.EDITOR));
        }
        if (userRepository.findByUsername("reviewer").isEmpty()) {
            createUser("reviewer", "reviewerpass", Set.of(Role.REVIEWER));
        }
    }
}
