package com.streamverse.service;

import com.streamverse.dto.*;
import com.streamverse.entity.User;
import com.streamverse.repository.UserRepository;
import com.streamverse.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    public AuthResponse login(AuthRequest req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("Account is " + user.getStatus().name().toLowerCase());
        }

        return AuthResponse.builder()
            .token(jwtUtil.generateToken(user))
            .user(UserDto.from(user))
            .build();
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        User user = User.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail())
            .username(req.getUsername())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(req.getRole() != null ? req.getRole() : User.Role.VIEWER)
            .status(User.UserStatus.ACTIVE)
            .build();

        userRepository.save(user);

        return AuthResponse.builder()
            .token(jwtUtil.generateToken(user))
            .user(UserDto.from(user))
            .build();
    }
}
