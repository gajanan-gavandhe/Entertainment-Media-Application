package com.streamverse.dto;
import com.streamverse.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 3) private String username;
    @NotBlank @Size(min = 8) private String password;
    private User.Role role = User.Role.VIEWER;
}
