package com.streamverse.controller;

import com.streamverse.dto.*;
import com.streamverse.entity.User;
import com.streamverse.repository.ViewHistoryRepository;
import com.streamverse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ViewHistoryRepository viewHistoryRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMe(ud.getUsername())));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@AuthenticationPrincipal UserDetails ud) {
        User user = userService.getByEmail(ud.getUsername());
        long watched = viewHistoryRepository.countByUser(user);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("watched", watched)));
    }
}
