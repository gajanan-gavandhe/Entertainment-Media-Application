package com.streamverse.controller;

import com.streamverse.dto.*;
import com.streamverse.entity.*;
import com.streamverse.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ContentService contentService;
    private final StatsService statsService;

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getAdminStats()));
    }

    @GetMapping("/users")
    public ResponseEntity<?> users(
        @RequestParam(required = false) String role,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.ok(userService.adminGetUsers(role, status, q, page, size)));
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest req) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(userService.adminCreateUser(req)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> setStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            User.UserStatus status = User.UserStatus.valueOf(body.get("status"));
            return ResponseEntity.ok(ApiResponse.ok(userService.adminSetStatus(id, status)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> setRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            User.Role role = User.Role.valueOf(body.get("role"));
            return ResponseEntity.ok(ApiResponse.ok(userService.adminSetRole(id, role)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.adminDeleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/content")
    public ResponseEntity<?> content(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.ok(contentService.adminGetAll(status, page, size)));
    }

    @PutMapping("/content/{id}/status")
    public ResponseEntity<?> setContentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Content.ContentStatus status = Content.ContentStatus.valueOf(body.get("status"));
            return ResponseEntity.ok(ApiResponse.ok(contentService.adminSetStatus(id, status)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable Long id) {
        try {
            contentService.adminDeleteById(id);
            return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
