package ru.kovalev.shopping.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.CartState;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
class ShoppingServiceImplTest {
    @InjectMocks
    private ShoppingServiceImpl shoppingService;
    @Mock
    private QuantityUpdateService quantityUpdateDelegate;
    @Mock
    private CartRepository cartRepository;
    private Customer customer = createCustomer();
    private Product product = new Product();
    private Cart cart = new Cart();
    private Item item = new Item();

    @Test
    void getCustomersCart_ifPresent() {
        when(cartRepository.findFirstByCustomerIdAndDeletedIsFalse(customer.getId()))
                .thenReturn(Optional.of(cart));
        assertSame(cart, shoppingService.getCustomersCart(customer));
    }

    @Test
    void getCustomersCart_ifAbsent() {
        when(cartRepository.findFirstByCustomerIdAndDeletedIsFalse(customer.getId()))
                .thenReturn(Optional.empty());
        var newCart = shoppingService.getCustomersCart(customer);

        verify(cartRepository).save(newCart);
        assertThat(newCart.getCustomer()).isEqualTo(customer);
    }

    @Test
    void addItemToCart() {
        var quantity = ThreadLocalRandom.current().nextInt();

        var result = shoppingService.addItemToCart(cart, product, quantity);
        verify(quantityUpdateDelegate).updateQuantity(cart, product, quantity);
        assertSame(cart, result);
    }

    @Test
    void removeItemFromCart() {
        when(quantityUpdateDelegate.removeItemFromCart(cart, product))
                .thenReturn(item);
        assertSame(item, shoppingService.removeItemFromCart(cart, product));
    }

    @Test
    void updateQuantity() {
        var quantity = ThreadLocalRandom.current().nextInt();

        when(quantityUpdateDelegate.updateQuantity(cart, product, quantity))
                .thenReturn(item);
        assertSame(item, shoppingService.updateQuantity(cart, product, quantity));
    }

    @Test
    void order() {
        assertFalse(cart.isDeleted());
        assertThat(cart.getCartState()).isSameAs(CartState.CART_IDLE);
        doAnswer(AdditionalAnswers.<Cart>answerVoid((cart -> cart.setDeleted(true))))
                .when(cartRepository).softDelete(cart);

        assertTrue(shoppingService.order(cart));
        assertTrue(cart.isDeleted());
        assertThat(cart.getCartState()).isSameAs(CartState.CART_SUBMITTED);
    }

    private Customer createCustomer() {
        var customer = new Customer();
        customer.setId(UUID.randomUUID());
        return customer;
    }
}