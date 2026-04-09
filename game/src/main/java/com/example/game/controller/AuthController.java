package com.example.game.controller;

import com.example.game.dto.response.LoginResponse;
import com.example.game.dto.request.LoginUserRequest;
import com.example.game.dto.request.RegisterUserRequest;
import com.example.game.entity.User;
import com.example.game.security.JwtService;
import com.example.game.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/auth")
@RestController
@CrossOrigin(origins ={"http://localhost:5173", "http://localhost:3000"})
@AllArgsConstructor
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "login", description = "Authenticate user and return JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginUserRequest loginUserDto) {
        User user = authService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(user);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up", description = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registered"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest registerUserDto){
        User user = authService.signup(registerUserDto);
        return ResponseEntity.ok(Map.of("id", user.getId(), "Username", user.getUsername()));
    }
}
