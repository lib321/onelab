package com.user_service.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_service.user_service.config.SecurityConfig;
import com.user_service.user_service.controller.AuthController;
import com.user_service.user_service.model.Users;
import com.user_service.user_service.service.TokenService;
import com.user_service.user_service.service.UserDetailsServiceImpl;
import com.user_service.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AuthController.class})
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void rootWhenUnauthenticatedThen401() throws Exception {
        this.mvc.perform(get("/auth/home"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUserReturnsUser() throws Exception {
        Users user = new Users();
        user.setUsername("testUser");
        user.setPassword("password");

        when(userService.register(any(Users.class))).thenReturn(user);

        this.mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void validateUserReturnsOk() throws Exception {
        this.mvc.perform(get("/auth/validate").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void validateRoleReturnsTrueForMatchingRole() throws Exception {
        this.mvc.perform(get("/auth/validate-role")
                        .param("role", "ROLE_USER")
                        .with(user("testUser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validateRoleReturnsFalseForNonMatchingRole() throws Exception {
        this.mvc.perform(get("/auth/validate-role")
                        .param("role", "ROLE_ADMIN")
                        .with(user("testUser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void homeReturnsAuthenticatedUser() throws Exception {
        this.mvc.perform(get("/auth/home")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))
                                .authorities(new SimpleGrantedAuthority("USER"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, testUser role [USER]"));
    }

    @Test
    void tokenReturnsGeneratedToken() throws Exception {
        when(userDetailsService.loadUserByUsername("testUser"))
                .thenReturn(new User("testUser", new BCryptPasswordEncoder().encode("password"),
                        List.of(new SimpleGrantedAuthority("USER"))));

        when(tokenService.generateToken(any(Authentication.class)))
                .thenReturn("mocked-token");

        this.mvc.perform(post("/auth/token")
                        .with(httpBasic("testUser", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("mocked-token"));
    }
}
