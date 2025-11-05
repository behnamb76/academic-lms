package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.PersonDTO;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.repository.MajorRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PersonMapper implements BaseMapper<Person, PersonDTO> {
    @Autowired
    private MajorRepository majorRepository;

    public abstract Person toEntity(PersonDTO dto);

    public abstract PersonDTO toDto(Person person);

    @AfterMapping
    protected void afterToEntity(PersonDTO dto, @MappingTarget Person person) {
        if (dto.getMajorName() != null) {
            Major major = majorRepository
                    .findByName(dto.getMajorName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Major with name " + dto.getMajorName() + " not found"
                    ));
            person.setMajor(major);
        }
    }

    @AfterMapping
    protected void afterToDTO(Person person, @MappingTarget PersonDTO dto) {
        if (person.getMajor().getName() != null) {
            Major major = majorRepository
                    .findByName(person.getMajor().getName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Major with name " + dto.getMajorName() + " not found"
                    ));
            dto.setMajorName(major.getName());
        }
    }
}
