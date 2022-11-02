package ru.kovalev.shopping.service;

import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;

public interface ShoppingService extends QuantityUpdateService {
    Cart addItemToCart(Cart cart, Product product, int quantity);

    boolean removeItemFromCart(Cart cart, Product product);

    Item updateQuantity(Cart cart, Product product, int quantityChange);

    boolean order(Cart cart);

    Cart getCustomersCart(Customer customer);
}
