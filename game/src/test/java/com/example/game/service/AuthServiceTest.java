package com.example.game.service;

import com.example.game.dto.request.LoginUserRequest;
import com.example.game.dto.request.RegisterUserRequest;
import com.example.game.entity.User;
import com.example.game.exceptions.UserNotFoundException;
import com.example.game.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private AuthService authService;

    @Test
    void signupEncodesPasswordAndPersistsUser() {
        RegisterUserRequest request = new RegisterUserRequest("gabe", "password123");
        User savedUser = new User();
        savedUser.setId(42L);
        savedUser.setUsername("gabe");
        savedUser.setPasswordHash("encoded-password");

        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        User result = authService.signup(request);

        assertThat(result).isSameAs(savedUser);
        verify(passwordEncoder).encode("password123");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void authenticateReturnsUserAfterDelegatingToAuthenticationManager() {
        LoginUserRequest request = new LoginUserRequest("gabe", "password123");
        User user = new User();
        user.setId(7L);
        user.setUsername("gabe");
        user.setPasswordHash("encoded");

        when(userRepo.findByUsername("gabe")).thenReturn(Optional.of(user));

        User result = authService.authenticate(request);

        assertThat(result).isSameAs(user);
        verify(authenticationManager)
                .authenticate(new UsernamePasswordAuthenticationToken("gabe", "password123"));
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
