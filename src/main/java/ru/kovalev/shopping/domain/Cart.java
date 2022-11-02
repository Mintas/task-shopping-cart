package ru.kovalev.shopping.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@RequiredArgsConstructor
public class Cart extends BaseEntity {

    @NotNull
    @OneToMany(mappedBy = "cart", fetch = FetchType.EAGER)
    private Set<Item> items = new HashSet<>();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "customer_ID")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CartState cartState = CartState.CART_IDLE;
}