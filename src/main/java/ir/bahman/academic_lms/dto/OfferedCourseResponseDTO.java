package ir.bahman.academic_lms.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferedCourseResponseDTO {
    private LocalTime startTime;
    private LocalTime endDate;
    private Integer capacity;
    private String location;
    private String courseTitle;
    private String teacherName;
    private String majorName;
    private Long termId;
}
