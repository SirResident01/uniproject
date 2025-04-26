package com.example.movies.controller;

import com.example.movies.model.Role;
import com.example.movies.model.User;
import com.example.movies.repository.RoleRepository;
import com.example.movies.repository.UserRepository;
import com.example.movies.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Operation(summary = "User Registration", description = "Registers a new user. Доступен для: все")
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email) {
        logger.info("Attempting to register user: {}", username);
        try {
            if (userRepository.findByUsername(username) != null) {
                return "Пользователь с таким именем уже существует!";
            }

            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                userRole = new Role("ROLE_USER");
                roleRepository.save(userRole);
            }

            User newUser = new User(username, passwordEncoder.encode(password));
            newUser.setEmail(email);
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            newUser.setRoles(roles);

            userRepository.save(newUser);

            logger.info("User {} registered successfully", username);
            String token = jwtUtils.generateToken(newUser.getUsername());

            return token;
        } catch(Exception e) {
            logger.error("Error during user registration for {}: {}", username, e.getMessage(), e);
            return "Registration failed: " + e.getMessage();
        }
    }

    @Operation(summary = "User Login", description = "Authenticates user and returns JWT. Доступен для: все")
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password) {
        logger.info("Attempting login for user: {}", username);
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return "Неверные данные (user not found)";
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return "Неверный пароль";
            }

            String token = jwtUtils.generateToken(user.getUsername());
            logger.info("User {} logged in successfully", username);
            return token;
        } catch(Exception e) {
            logger.error("Error during login for {}: {}", username, e.getMessage(), e);
            return "Login failed: " + e.getMessage();
        }
    }

    @Operation(summary = "Admin Registration", description = "Registers a new admin. Доступен для: все (использовать только при инициализации)")
    @PostMapping("/register-admin")
    public String registerAdmin(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String email) {
        logger.info("Attempting to register admin: {}", username);
        try {
            if (userRepository.findByUsername(username) != null) {
                return "Пользователь с таким именем уже существует!";
            }

            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new Role("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }

            User newUser = new User(username, passwordEncoder.encode(password));
            newUser.setEmail(email);
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            newUser.setRoles(roles);
            userRepository.save(newUser);

            logger.info("Admin {} registered successfully", username);
            String token = jwtUtils.generateToken(newUser.getUsername());
            return token;
        } catch(Exception e) {
            logger.error("Error during admin registration for {}: {}", username, e.getMessage(), e);
            return "Admin registration failed: " + e.getMessage();
        }
    }
}
