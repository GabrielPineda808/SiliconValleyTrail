package com.example.game.service;

import com.example.game.dto.request.LoginUserRequest;
import com.example.game.dto.request.RegisterUserRequest;
import com.example.game.dto.response.LoginResponse;
import com.example.game.dto.response.RegisterUserResponse;
import com.example.game.entity.User;
import com.example.game.exceptions.UserNotFoundException;
import com.example.game.repository.UserRepo;
import com.example.game.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.AuthProvider;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void signupEncodesPasswordAndPersistsUser() {
        RegisterUserRequest request = new RegisterUserRequest("gabe", "password123");
        User savedUser = new User();
        savedUser.setId(Long.valueOf(42L));
        savedUser.setUsername("gabe");
        savedUser.setPasswordHash("encoded-password");

        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        RegisterUserResponse result = authService.signup(request);

        assertThat(result).isSameAs(new RegisterUserResponse(savedUser.getId(),savedUser.getUsername()));
        verify(passwordEncoder).encode("password123");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void authenticateReturnsUserAfterDelegatingToAuthenticationManager() {
        LoginUserRequest request = new LoginUserRequest("gabe", "password123");
        User user = new User();
        user.setId(Long.valueOf(7L));
        user.setUsername("gabe");
        user.setPasswordHash("encoded");

        when(userRepo.findByUsername("gabe")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(Long.valueOf(3600000L));

        LoginResponse result = authService.authenticate(request);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("mock-jwt-token");
        assertThat(result.expiresIn()).isEqualTo(3600000L);

        verify(authenticationManager).authenticate(argThat(auth ->
                auth instanceof UsernamePasswordAuthenticationToken &&
                        Objects.equals(auth.getPrincipal(), "gabe") &&
                        Objects.equals(auth.getCredentials(), "password123")
        ));
        verify(jwtService).generateToken(user);
        verify(jwtService).getExpirationTime();
    }

    @Test
    void authenticateThrowsWhenUserDoesNotExist() {
        LoginUserRequest request = new LoginUserRequest("missing", "password123");
        when(userRepo.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }
}
