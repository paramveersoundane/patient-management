package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private  final UserService userService;
    private final PasswordEncoder passwordEncoder;
    public AuthService(UserService userService, PasswordEncoder passwordEncoder){
        this.userService=userService;
        this.passwordEncoder=passwordEncoder;
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO){
        Optional<String> token = userService.findByEmail(loginRequestDTO.getEmail())  //find email
                .filter(u->passwordEncoder.matches(loginRequestDTO.getPassword(),u.getPassword()))  // encoded password matches
                .map(u-> jwtUtil.generateToken(u.getEmail(),u.getPassword()));    // generate token

        return token;
    }
}
