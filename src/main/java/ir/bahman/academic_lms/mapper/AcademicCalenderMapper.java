package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.AcademicCalenderDTO;
import ir.bahman.academic_lms.model.AcademicCalender;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class AcademicCalenderMapper implements BaseMapper<AcademicCalender, AcademicCalenderDTO> {
}
