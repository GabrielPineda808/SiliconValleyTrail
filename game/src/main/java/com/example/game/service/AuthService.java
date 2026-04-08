package com.example.game.service;

import com.example.game.dto.LoginUserDto;
import com.example.game.dto.RegisterUserDto;
import com.example.game.entity.User;
import com.example.game.exceptions.UserNotFoundException;
import com.example.game.repository.UserRepo;
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

    @Transactional
    public User signup(RegisterUserDto input){
        User user =new User();
        user.setPasswordHash(passwordEncoder.encode(input.password()));
        user.setUsername(input.username());
        return userRepo.save(user);
    }

    public User authenticate(LoginUserDto input){
        User user = userRepo.findByUsername(input.username()).orElseThrow(()-> new UserNotFoundException("User not found"));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.username(),input.password()));
        return user;
    }
}
