package ru.kovalev.shopping.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import ru.kovalev.shopping.BaseIntegrationTest;
import ru.kovalev.shopping.domain.Item;

@TestPropertySource(properties = {
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type=TRACE",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class ShoppingServiceImplIntegrationTest extends BaseIntegrationTest {

    @Test
    void addItemToCart() {
        var customer = createCustomer();
        var product = productSomething10();

        var quantity = 5;
        var buyerCart = shoppingService.getCustomersCart(customer);
        var cart = shoppingService.addItemToCart(buyerCart, product, quantity);

        assertThat(product.getReserved()).isEqualTo(product.getStored() - quantity);
        assertThat(cart.getItems()).hasSize(1)
                .first().extracting(Item::getProduct, Item::getQuantity, Item::getCart)
                .containsExactly(product, quantity, cart);

        var p2 = productRepository.findById(product.getId());
        var cart2 = shoppingService.addItemToCart(cart, p2.get(), quantity);
        System.out.println("wow");
    }


}