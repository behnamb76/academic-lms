package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.exception.AlreadyExistsException;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.repository.MajorRepository;
import ir.bahman.academic_lms.service.MajorService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MajorServiceImpl extends BaseServiceImpl<Major,Long> implements MajorService {
    private final MajorRepository majorRepository;

    public MajorServiceImpl(JpaRepository<Major, Long> repository, MajorRepository majorRepository) {
        super(repository);
        this.majorRepository = majorRepository;
    }

    @Override
    protected void prePersist(Major major) {
        if (majorRepository.findByName(major.getName()).isPresent()){
            throw new AlreadyExistsException("This major already exists!");
        }
        major.setMajorCode(UUID.randomUUID());
        major.setDeleted(false);
    }

    @Override
    public void deleteById(Long id) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Major not found"));
        major.setDeleted(true);
        majorRepository.save(major);
    }

    @Override
    public List<Major> findAll() {
        return majorRepository.findByDeletedIsFalse();
    }

    @Override
    public Major update(Long id, Major major) {
        Major foundedMajor = majorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Major not found"));
        if(majorRepository.findByName(major.getName()).isPresent()){
            throw new AlreadyExistsException("This major already exists!");
        }
        foundedMajor.setName(major.getName());
        return majorRepository.save(foundedMajor);
    }

    @Override
    public Major findById(Long id) {
        return majorRepository.findByDeletedIsFalseAndId(id)
                .orElseThrow(() -> new EntityNotFoundException("Major not found"));
    }
}
