package ir.bahman.academic_lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username is required!")
    private String username;

    @NotBlank(message = "Username is required!")
    private String password;
}
