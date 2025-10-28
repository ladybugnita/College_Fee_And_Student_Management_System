package com.example.collegefeeandstudentmanagement.service;

import com.example.collegefeeandstudentmanagement.dto.LoginRequest;
import com.example.collegefeeandstudentmanagement.dto.LoginResponse;
import com.example.collegefeeandstudentmanagement.entity.Role;
import com.example.collegefeeandstudentmanagement.entity.TokenBlacklist;
import com.example.collegefeeandstudentmanagement.entity.User;
import com.example.collegefeeandstudentmanagement.repository.BlacklistTokenRepository;
import com.example.collegefeeandstudentmanagement.repository.UserRepository;
import com.example.collegefeeandstudentmanagement.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final JwtUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       BlacklistTokenRepository blacklistTokenRepository,
                       JwtUtil jwtTokenUtil,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.blacklistTokenRepository = blacklistTokenRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(String username, String password, String roleName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        Role role;
        try {
            role = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + roleName);
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String role = user.getRoles().iterator().next().name();
        String token = jwtTokenUtil.generateToken(username, role);

        return new LoginResponse(token, username, role);
    }

    public void logout(String token) {
        if(token == null) return;
        if(token.toLowerCase().startsWith("bearer ")){
            token = token.substring(7);
        }
        TokenBlacklist blacklisted = new TokenBlacklist();
        blacklisted.setToken(token);
        blacklistTokenRepository.save(blacklisted);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistTokenRepository.findByToken(token).isPresent();
    }
}
