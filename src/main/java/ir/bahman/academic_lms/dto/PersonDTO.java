package ir.bahman.academic_lms.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonDTO {
    @NotBlank(message = "First name is required!")
    @Size(max = 50, message = "firstname must be {max} characters maximum!")
    private String firstName;

    @NotBlank(message = "Last name is required!")
    @Size(max = 50, message = "lastname must be {max} characters maximum!")
    private String lastName;

    @NotBlank(message = "National code is required!")
    @Pattern(regexp = "\\d{10}", message = "National code must be exactly 10 digits with no whitespace")
    private String nationalCode;

    @NotBlank(message = "Phone number is required!")
    @Pattern(regexp = "09\\d{9}", message = "Phone number must be 11 digits starting with 09 (e.g., 09123456789)")
    private String phoneNumber;

    @NotBlank(message = "Major name is required!")
    private String majorName;
}
