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
import ru.kovalev.shopping.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ShoppingServiceImpl implements ShoppingService {
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final QuantityUpdateService itemUpdatingService;
    private final QuantityUpdateService itemSkippingService;

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
        itemUpdatingService.updateQuantity(cart, product, quantity);
        return cart;
    }

    @Override
    @Transactional
    public boolean removeItemFromCart(Cart cart, Product product) {
        var item = itemSkippingService.updateQuantity(cart, product, Integer.MIN_VALUE);
        itemRepository.softDelete(item);
        return true;
    }

    @Override
    @Transactional
    public boolean order(Cart cart) {
        cart.setCartState(CartState.CART_SUBMITTED);
        cartRepository.softDelete(cart);
        return true;
    }

    @Override
    @Transactional
    public Item updateQuantity(Cart cart, Product product, int quantity) {
        return itemUpdatingService.updateQuantity(cart, product, quantity);
    }
}
