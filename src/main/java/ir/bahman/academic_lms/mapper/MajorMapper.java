package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.MajorDTO;
import ir.bahman.academic_lms.model.Major;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class MajorMapper implements BaseMapper<Major, MajorDTO> {
}
