package com.example.game.controller;

import com.example.game.dto.response.LoginResponse;
import com.example.game.dto.request.LoginUserRequest;
import com.example.game.dto.request.RegisterUserRequest;
import com.example.game.dto.response.RegisterUserResponse;

import com.example.game.exceptions.ErrorResponse;

import com.example.game.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for user registration and authentication.
 */
@RequestMapping("/auth")
@RestController
@CrossOrigin(origins ={"http://localhost:5173", "http://localhost:3000"})
@AllArgsConstructor
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {
    private final AuthService authService;

    /**
     * Authenticates an existing user and returns a signed JWT.
     *
     * @param loginUserDto validated login credentials
     * @return JWT token and expiration metadata
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns a signed JWT bearer token.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authenticated successfully.",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User was not found.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginUserRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    /**
     * Registers a new user account.
     *
     * @param registerUserDto validated registration payload
     * @return summary of the newly created account
     */
    @PostMapping("/signup")
    @Operation(summary = "Sign Up", description = "Registers a new user account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully.",
                    content = @Content(schema = @Schema(implementation = RegisterUserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest registerUserDto){
        return ResponseEntity.ok(authService.signup(registerUserDto));
    }
}
