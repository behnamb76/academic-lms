package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.model.base.BaseEntity;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public interface BaseService<T extends BaseEntity<ID>, ID extends Serializable> {
    T persist(T t);
    T update(ID id, T t);
    void deleteById(ID id);
    T findById(ID id);
    List<T> findAll();
}
