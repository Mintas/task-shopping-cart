package ru.kovalev.shopping.scheduled;

import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.kovalev.shopping.BaseIntegrationTest;
import ru.kovalev.shopping.domain.CartState;

@TestPropertySource(properties = {
        "shopping.cart.expiration.ttl=450ms",
        "shopping.cart.expiration.routine-delay=300ms"
})
class CartExpirationRoutineTest extends BaseIntegrationTest {
    @Value("${shopping.cart.expiration.routine-delay}")
    Duration delay;
    @Value("${shopping.cart.expiration.ttl}")
    Duration expirationInterval;

    @Test
    void expirationRoutine() {
        var customer = createCustomer();
        var cart = shoppingService.getCustomersCart(customer);
        assertThat(cart.getCartState()).isSameAs(CartState.CART_IDLE);

        Awaitility.await().atLeast(delay)
                .atMost(delay.plus(expirationInterval))
                .until(() -> !shoppingService.getCustomersCart(customer).equals(cart));

        var carts = cartRepository.findAllByCustomerId(customer.getId());
        assertThat(carts).hasSize(2)
                .first().isEqualTo(cart)
                .satisfies(c1 -> {
                    assertThat(c1.getCartState()).isSameAs(CartState.CART_OBSOLETE);
                    assertThat(c1.getCreatedAt()).isEqualTo(cart.getCreatedAt());
                    assertThat(c1.getUpdatedAt()).isBetween(c1.getCreatedAt(),
                            c1.getCreatedAt().plus(delay.plus(expirationInterval)));
                });
        assertThat(carts.get(1)).isNotEqualTo(cart)
                .satisfies(c2 -> {
                    assertThat(c2.getCartState()).isSameAs(CartState.CART_IDLE);
                    assertThat(c2.getCreatedAt()).isAfter(carts.get(0).getUpdatedAt());
                });
    }
}