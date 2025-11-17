package ir.bahman.academic_lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionDTO {
    @NotBlank(message = "Option text is required")
    @Size(max = 100, message = "Option text should not exceed 100 characters")
    private String text;

    private boolean correct;
}
