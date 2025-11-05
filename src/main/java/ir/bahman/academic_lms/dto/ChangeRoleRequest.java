package ir.bahman.academic_lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRoleRequest {
    @NotBlank(message = "Role name required")
    private String role;
}
