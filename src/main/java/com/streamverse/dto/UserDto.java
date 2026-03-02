package com.streamverse.dto;
import com.streamverse.entity.User;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String username;
    private User.Role role;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    public static UserDto from(User u) {
        return UserDto.builder()
            .id(u.getId()).firstName(u.getFirstName()).lastName(u.getLastName())
            .fullName(u.getFullName()).email(u.getEmail()).username(u.getUsername())
            .role(u.getRole()).status(u.getStatus()).createdAt(u.getCreatedAt()).build();
    }
}
