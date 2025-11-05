package ir.bahman.academic_lms.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
