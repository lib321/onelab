package com.user_service.user_service.controller;

import com.user_service.user_service.model.Users;
import com.user_service.user_service.service.TokenService;
import com.user_service.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);


    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public Users register(@RequestBody Users user) {
        return userService.register(user);
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateUser() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate-role")
    public ResponseEntity<Boolean> validateUserRole(Principal principal, @RequestParam String role) {
        Authentication authentication = (Authentication) principal;
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));

        return ResponseEntity.ok(hasRole);
    }

    @GetMapping("/home")
    public String home(Principal principal) {
        Authentication authentication = (Authentication) principal;
        return "Hello, " + principal.getName() + " role " + authentication.getAuthorities();
    }

    @PostMapping("/token")
    public String token(Authentication authentication) {
        LOG.debug("Token requested for user: '{}'", authentication.getName());
        String token = tokenService.generateToken(authentication);
        LOG.debug("Token granted: {}", token);
        return token;
    }
}
