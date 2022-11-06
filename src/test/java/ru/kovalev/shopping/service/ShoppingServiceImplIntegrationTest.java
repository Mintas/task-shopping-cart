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
        var firstItemAdded = cart.getItems().iterator().next();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(firstItemAdded)
                .extracting(Item::getProduct, Item::getQuantity, Item::getCart)
                .containsExactly(product, quantity, cart);

        var productUpdated = productRepository.findById(product.getId()).get();
        var cart2 = shoppingService.addItemToCart(cart, productUpdated, quantity);

        assertThat(productUpdated)
                .isEqualTo(product)
                .usingRecursiveComparison().isNotEqualTo(product);
        var newQuantity = quantity * 2;
        assertThat(productUpdated.getAvailableQuantity())
                .isEqualTo(product.getStored() - newQuantity)
                .isEqualTo(product.getReserved() - quantity);
        assertThat(productUpdated.getReserved())
                .isEqualTo(newQuantity);
        assertThat(cart2)
                .isEqualTo(cart)
                .usingRecursiveComparison().isEqualTo(cart);
        assertThat(cart2.getItems()).hasSize(1)
                .first()
                .isEqualTo(firstItemAdded)
                .extracting(Item::getProduct, Item::getQuantity, Item::getCart, Item::getVersion)
                .containsExactly(productUpdated, newQuantity, cart2, firstItemAdded.getVersion() + 1);
    }
}