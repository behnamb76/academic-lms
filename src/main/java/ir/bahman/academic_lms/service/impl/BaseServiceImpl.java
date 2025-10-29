package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.model.base.BaseEntity;
import ir.bahman.academic_lms.service.BaseService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

public abstract class BaseServiceImpl<T extends BaseEntity<ID>, ID extends Serializable> implements BaseService<T, ID> {
    private final JpaRepository<T, ID> repository;

    protected BaseServiceImpl(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public T persist(T t) {
        prePersist(t);
        T saved = repository.save(t);
        postPersist(saved);
        return saved;
    }

    @Override
    public abstract T update(ID id, T t);

    @Override
    public void deleteById(ID id) {
        preDelete(id);
        T t = repository.findById(id).orElseThrow();
        repository.delete(t);
        postDelete(id);
    }

    @Override
    public T findById(ID id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    protected void prePersist(T t) {
    }

    protected void postPersist(T t) {
    }

    protected void preDelete(ID id) {
    }

    protected void postDelete(ID id) {
    }

}
