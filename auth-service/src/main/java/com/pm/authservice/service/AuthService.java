package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserService userService;
    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO){
        Optional<User> user = userService.findByEmail(loginRequestDTO.getEmail());
        // wip
        return null;
    }
}
