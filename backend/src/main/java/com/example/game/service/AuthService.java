package com.example.game.service;

import com.example.game.dto.request.LoginUserRequest;
import com.example.game.dto.request.RegisterUserRequest;
import com.example.game.dto.response.LoginResponse;
import com.example.game.dto.response.RegisterUserResponse;
import com.example.game.entity.User;
import com.example.game.exceptions.UserNotFoundException;
import com.example.game.repository.UserRepo;
import com.example.game.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@AllArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public RegisterUserResponse signup(RegisterUserRequest input){
        User user =new User();
        user.setPasswordHash(passwordEncoder.encode(input.password()));
        user.setUsername(input.username());
        userRepo.save(user);
        return new RegisterUserResponse(user.getId(), user.getUsername());
    }

    public LoginResponse authenticate(LoginUserRequest input){
        User user = userRepo.findByUsername(input.username()).orElseThrow(()-> new UserNotFoundException("User not found"));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.username(),input.password()));
        String jwtToken = jwtService.generateToken(user);
        return new LoginResponse(jwtToken,jwtService.getExpirationTime());
    }
}
