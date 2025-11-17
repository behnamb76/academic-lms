package ir.bahman.academic_lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {
    @NotBlank(message = "Course title cannot be empty")
    @Size(min = 3, max = 30, message = "Title must be between 3 and 30 characters")
    private String title;

    @NotNull(message = "Unit cannot be empty")
    @Min(value = 1, message = "Unit must be at least 1")
    private Integer unit;

    @Size(max = 50, message = "Description should not exceed 50 characters")
    private String description;

    @NotBlank(message = "Major name cannot be empty")
    private String majorName;
}
