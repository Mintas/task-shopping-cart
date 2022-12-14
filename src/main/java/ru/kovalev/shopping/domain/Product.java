package ru.kovalev.shopping.domain;

import javax.persistence.Entity;
import javax.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class Product extends BaseEntity {
    private String name;
    private String description;
    @PositiveOrZero
    private int stored;
    @PositiveOrZero
    private int reserved;

    public int getAvailableQuantity() {
        return stored - reserved;
    }
}