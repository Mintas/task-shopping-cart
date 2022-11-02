package ru.kovalev.shopping.service;

import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;

public interface QuantityUpdateService {
    Item updateQuantity(Cart cart, Product product, int quantity);
}
