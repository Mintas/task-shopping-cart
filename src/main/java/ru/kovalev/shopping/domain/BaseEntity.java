package ru.kovalev.shopping.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@Getter
@ToString
@Setter
@RequiredArgsConstructor
public abstract class BaseEntity {
    public static final String DELETED_FIELD = "deleted";

    @Id
    @GeneratedValue
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

//    @CreatedBy
//    @Column(name = "created_by", updatable = false)
//    private String createdBy;

    @UpdateTimestamp
    private Instant updatedAt;

    private boolean deleted;

    @Version
    private Integer version;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var entity = (BaseEntity) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
