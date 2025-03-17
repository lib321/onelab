package com.user_service.user_service.service;

import com.user_service.user_service.model.Role;
import com.user_service.user_service.model.Users;
import com.user_service.user_service.repository.RoleRepository;
import com.user_service.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public Users register(Users user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

        Set<Role> savedRoles = user.getRoles().stream()
                .map(role -> roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("Роль не найдена: " + role.getName())))
                .collect(Collectors.toSet());

        user.setRoles(savedRoles);
        return userRepository.save(user);
    }
}
