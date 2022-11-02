package ru.kovalev.shopping.repository;

import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import static ru.kovalev.shopping.domain.BaseEntity.DELETED_FIELD;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.util.Assert;
import ru.kovalev.shopping.domain.BaseEntity;

@NoRepositoryBean
public class SoftDeleteRepositoryImpl<T extends BaseEntity, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements SoftDeleteRepository<T, ID> {
    public SoftDeleteRepositoryImpl(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public Iterable<T> findAllActive() {
        return super.findAll(isActive());
    }

    @Override
    public void softDelete(T entity) {
        Assert.notNull(entity, "Entity must not be null!");
        entity.setDeleted(true);
        this.save(entity);
    }

    private static <T> Specification<T> isActive() {
        return Specification.where(new IsActive<>());
    }

    private static final class IsActive<T> implements Specification<T> {
        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.isFalse(root.get(DELETED_FIELD));
        }
    }
}
