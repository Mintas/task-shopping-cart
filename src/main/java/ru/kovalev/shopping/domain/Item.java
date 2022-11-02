package ru.kovalev.shopping.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class Item extends BaseEntity {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "cart_id")
    @ToString.Exclude
    private Cart cart;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @PositiveOrZero
    private int quantity;

    public Item addQuantity(int additional) {
        quantity += additional;
        return this;
    }
}
