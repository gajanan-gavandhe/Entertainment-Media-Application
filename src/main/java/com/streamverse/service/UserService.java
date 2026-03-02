package com.streamverse.service;

import com.streamverse.dto.*;
import com.streamverse.entity.User;
import com.streamverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto getMe(String email) {
        return UserDto.from(getByEmail(email));
    }

    // ADMIN
    public PageResponse<UserDto> adminGetUsers(String role, String status, String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        User.Role roleEnum = (role != null && !role.isBlank()) ? User.Role.valueOf(role) : null;
        User.UserStatus statusEnum = (status != null && !status.isBlank()) ? User.UserStatus.valueOf(status) : null;
        Page<User> result = userRepository.findWithFilters(roleEnum, statusEnum, q, pageable);
        return PageResponse.from(result.map(UserDto::from));
    }

    @Transactional
    public UserDto adminCreateUser(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) throw new RuntimeException("Email already exists");
        if (userRepository.existsByUsername(req.getUsername())) throw new RuntimeException("Username taken");
        User user = User.builder()
            .firstName(req.getFirstName()).lastName(req.getLastName())
            .email(req.getEmail()).username(req.getUsername())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(req.getRole() != null ? req.getRole() : User.Role.VIEWER)
            .status(User.UserStatus.ACTIVE).build();
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public UserDto adminSetStatus(Long id, User.UserStatus status) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public UserDto adminSetRole(Long id, User.Role role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public void adminDeleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
