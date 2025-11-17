package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.OptionDTO;
import ir.bahman.academic_lms.model.Option;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OptionMapper extends BaseMapper<Option, OptionDTO> {
}
