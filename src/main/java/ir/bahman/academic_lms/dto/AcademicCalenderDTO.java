package ir.bahman.academic_lms.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicCalenderDTO {
    @NotNull(message = "Course registration start date is required")
    @FutureOrPresent(message = "Course registration start date must be today or in the future.")
    private LocalDate registrationStart;

    @NotNull(message = "Course registration end date is required")
    @FutureOrPresent(message = "Course registration end date must be on or after the start date.")
    private LocalDate registrationEnd;

    @NotNull(message = "Classes start date is required")
    @FutureOrPresent(message = "Classes start date must be today or in the future.")
    private LocalDate classesStartDate;

    @NotNull(message = "Classes end date is required")
    @FutureOrPresent(message = "Classes end date should be today or in the future")
    private LocalDate classesEndDate;

    @AssertTrue(message = "Course registration end date must be on or after start date")
    public boolean isRegistrationDatesValid() {
        if (registrationStart == null || registrationEnd == null) return true;
        return !registrationEnd.isBefore(registrationStart);
    }

    @AssertTrue(message = "Classes end date must be on or after start date")
    public boolean isClassesDatesValid() {
        if (classesStartDate == null || classesEndDate == null) return true;
        return !classesEndDate.isBefore(classesStartDate);
    }

    @AssertTrue(message = "Classes must start on or after course registration ends")
    public boolean isClassesAfterRegistration() {
        if (registrationEnd == null || classesStartDate == null) return true;
        return !classesStartDate.isBefore(registrationEnd);
    }
}
