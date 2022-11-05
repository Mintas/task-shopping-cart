package ru.kovalev.shopping.service;

import javax.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.domain.Product;

@Validated
public interface ShoppingService extends QuantityUpdateService {
    Cart getCustomersCart(Customer customer);

    boolean order(@NotEmpty Cart cart);

    Cart addItemToCart(Cart cart, Product product, int quantity);
}
