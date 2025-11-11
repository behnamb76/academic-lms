package ir.bahman.academic_lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MajorDTO {
    @NotBlank(message = "Major name is required!")
    private String name;
}
