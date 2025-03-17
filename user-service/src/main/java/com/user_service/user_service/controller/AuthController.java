package com.user_service.user_service.controller;

import com.user_service.user_service.model.Users;
import com.user_service.user_service.principal.UserPrincipal;
import com.user_service.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Users register(@RequestBody Users user) {
        return userService.register(user);
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateUser() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate-role")
    public ResponseEntity<Boolean> validateUserRole(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                    @RequestParam String role) {
        boolean hasRole = userPrincipal.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));

        return ResponseEntity.ok(hasRole);
    }

}
