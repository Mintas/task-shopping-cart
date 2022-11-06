package ru.kovalev.shopping.usecase;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.exceptions.ShopEntityNotFoundProblem;
import ru.kovalev.shopping.repository.ProductRepository;
import ru.kovalev.shopping.service.CustomerService;
import ru.kovalev.shopping.service.ShoppingService;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingCartUseCase {
    private final CustomerService customerService;
    private final ShoppingService shoppingService;
    private final ProductRepository productRepository;

    public Cart fetchCart() {
        var customer = fetchCustomer();
        return shoppingService.getCustomersCart(customer);
    }

    public Cart addItemToCart(UUID productId, Integer quantity) {
        var product = fetchProduct(productId);
        return shoppingService.addItemToCart(fetchCart(), product, quantity);
    }

    public Cart removeItemFromCart(UUID productId) {
        var product = fetchProduct(productId);
        var cart = fetchCart();
        var yes = shoppingService.removeItemFromCart(cart, product);
        return cart;
    }

    public Item changeItemQuantity(UUID productId, Integer quantity) {
        var product = fetchProduct(productId);
        return shoppingService.updateQuantity(fetchCart(), product, quantity);
    }

    public Cart orderCart() {
        var cart = fetchCart();
        shoppingService.order(cart);
        return cart;
    }

    private Customer fetchCustomer() {
        return customerService.getCurrentCustomer();
    }

    private Product fetchProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ShopEntityNotFoundProblem(Product.class, productId));
    }
}
