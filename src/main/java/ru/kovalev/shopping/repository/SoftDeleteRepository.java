package ru.kovalev.shopping.repository;

import java.io.Serializable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface SoftDeleteRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

    Iterable<T> findAllActive();

    @Transactional
    void softDelete(T entity);
}
