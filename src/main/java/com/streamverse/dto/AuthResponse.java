package com.streamverse.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;
}
