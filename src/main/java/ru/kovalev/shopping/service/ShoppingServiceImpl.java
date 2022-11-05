package ru.kovalev.shopping.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.CartState;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.repository.CartRepository;

@Service
@RequiredArgsConstructor
public class ShoppingServiceImpl implements ShoppingService {
    private final CartRepository cartRepository;
    private final QuantityUpdateService quantityUpdateService;

    @Override
    public Cart getCustomersCart(Customer customer) {
        return cartRepository.findFirstByCustomerIdAndDeletedIsFalse(customer.getId())
                .orElseGet(() -> initCart(customer));
    }

    private Cart initCart(Customer customer) {
        var cart = new Cart();
        cart.setCustomer(customer);
        cartRepository.save(cart);
        return cart;
    }

    @Override
    @Transactional
    public Cart addItemToCart(Cart cart, Product product, int quantity) {
        quantityUpdateService.updateQuantity(cart, product, quantity);
        return cart;
    }

    @Override
    @Transactional
    public Item removeItemFromCart(Cart cart, Product product) {
        return quantityUpdateService.removeItemFromCart(cart, product);
    }

    @Override
    @Transactional
    public Item updateQuantity(Cart cart, Product product, int quantity) {
        return quantityUpdateService.updateQuantity(cart, product, quantity);
    }

    @Override
    @Transactional
    public boolean order(Cart cart) {
        cart.setCartState(CartState.CART_SUBMITTED);
        cartRepository.softDelete(cart);
        return true;
    }
}
