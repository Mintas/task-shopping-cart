package ru.kovalev.shopping.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.CartState;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
class CartExpirationServiceImplTest {
    private CartExpirationService cartExpirationService;
    @Mock
    private QuantityUpdateService quantityUpdateService;
    @Mock
    private CartRepository cartRepository;

    private final Instant now = Instant.now();
    private final Product product = new Product();
    private final Duration duration = Duration.ofHours(8);

    @BeforeEach
    void setUp() {
        cartExpirationService = new CartExpirationServiceImpl(cartRepository, quantityUpdateService, duration);
    }

    @Test
    void expireCart_expired() {
        var cart = cartUpdatedAt(now.minus(duration));

        var uuid = UUID.randomUUID();
        when(cartRepository.findByIdAndDeletedIsFalse(uuid))
                .thenReturn(Optional.of(cart));

        assertTrue(cartExpirationService.expireCart(uuid));

        verify(cartRepository).softDelete(cart);
        verify(quantityUpdateService, never()).updateQuantity(any(), any(), anyInt());
        assertThat(cart.getCartState()).isSameAs(CartState.CART_OBSOLETE);
    }

    @Test
    void expireCart_expiredCartAndItems() {
        var cart = cartUpdatedAt(now.minus(duration));
        var item1 = createAndAddItem(product, now.minus(duration), cart);

        var product2 = new Product();
        product2.setId(UUID.randomUUID());
        var item2 = createAndAddItem(product2, now.minus(duration), cart);
        item2.setQuantity(5);

        var uuid = UUID.randomUUID();
        when(cartRepository.findByIdAndDeletedIsFalse(uuid))
                .thenReturn(Optional.of(cart));

        assertTrue(cartExpirationService.expireCart(uuid));

        verify(cartRepository).softDelete(cart);
        verify(quantityUpdateService).updateQuantity(cart, product, item1.getQuantity());
        verify(quantityUpdateService).updateQuantity(cart, product2, -item2.getQuantity());
        assertThat(cart.getCartState()).isSameAs(CartState.CART_OBSOLETE);
    }

    @Test
    void expireCart_false_expiredItemButUpdatedCart() {
        var cart = cartUpdatedAt(now);
        createAndAddItem(product, now, cart);
        createAndAddItem(product, now.minus(duration), cart);

        var uuid = UUID.randomUUID();
        when(cartRepository.findByIdAndDeletedIsFalse(uuid))
                .thenReturn(Optional.of(cart));

        assertFalse(cartExpirationService.expireCart(uuid));

        verify(cartRepository, never()).softDelete(cart);
        verify(quantityUpdateService, never()).updateQuantity(cart, product, 0);
        assertThat(cart.getCartState()).isSameAs(CartState.CART_IDLE);
    }

    @Test
    void expireCart_false_expiredCartButUpdatedItem() {
        var cart = cartUpdatedAt(now);

        var item = new Item();
        item.setProduct(product);
        item.setUpdatedAt(now.minus(duration));
        cart.getItems().add(item);

        var uuid = UUID.randomUUID();
        when(cartRepository.findByIdAndDeletedIsFalse(uuid))
                .thenReturn(Optional.of(cart));

        assertFalse(cartExpirationService.expireCart(uuid));

        verify(cartRepository, never()).softDelete(cart);
        verify(quantityUpdateService, never()).updateQuantity(cart, product, 0);
        assertThat(cart.getCartState()).isSameAs(CartState.CART_IDLE);
    }

    private Cart cartUpdatedAt(Instant now) {
        var cart = new Cart();
        cart.setUpdatedAt(now);
        return cart;
    }

    private Item createAndAddItem(Product product, Instant now, Cart cart) {
        var item = new Item();
        item.setProduct(product);
        item.setUpdatedAt(now);
        cart.getItems().add(item);
        return item;
    }
}