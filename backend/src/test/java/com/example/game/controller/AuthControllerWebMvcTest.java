package com.example.game.controller;

import com.example.game.dto.request.LoginUserRequest;
import com.example.game.dto.request.RegisterUserRequest;
import com.example.game.dto.response.LoginResponse;
import com.example.game.dto.response.RegisterUserResponse;
import com.example.game.entity.User;
import com.example.game.security.JwtService;
import com.example.game.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginReturnsJwtPayload() throws Exception {
        User user = new User();
        user.setId(15L);
        user.setUsername("gabe");
        user.setPasswordHash("encoded");

        LoginResponse loginResponse = new LoginResponse("jwt-token", 3600L);

        when(authService.authenticate(new LoginUserRequest("gabe", "password123"))).thenReturn(loginResponse);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginUserRequest("gabe", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(authService).authenticate(new LoginUserRequest("gabe", "password123"));
    }

    @Test
    void signupReturnsCreatedUserSummary() throws Exception {
        User user = new User();
        user.setId(22L);
        user.setUsername("gabe");
        RegisterUserResponse response = new RegisterUserResponse(user.getId(), user.getUsername());

        when(authService.signup(new RegisterUserRequest("gabe", "password123"))).thenReturn(response);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterUserRequest("gabe", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22))
                .andExpect(jsonPath("$.Username").value("gabe"));
    }

    @Test
    void signupRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Username is required"));
    }

    @Test
    void signupRejectsTooShortFields() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'username' && @.message == 'Username must be between 3 and 15 characters')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'password' && @.message == 'Password must be between 8 and 128 characters')]").exists());
    }
}
