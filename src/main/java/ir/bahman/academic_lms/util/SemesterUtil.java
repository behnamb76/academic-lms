package ir.bahman.academic_lms.util;

import ir.bahman.academic_lms.model.enums.Semester;

import java.time.LocalDate;
import java.time.Month;

public class SemesterUtil {
    public static Semester currentSemester() {
        Month m = LocalDate.now().getMonth();
        if (m.getValue() >= 3 && m.getValue() <= 5) return Semester.SPRING;
        if (m.getValue() >= 6 && m.getValue() <= 8) return Semester.SUMMER;
        if (m.getValue() >= 9 && m.getValue() <= 11) return Semester.FALL;
        return Semester.WINTER;
    }
}
