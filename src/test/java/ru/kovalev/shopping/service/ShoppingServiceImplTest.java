package ru.kovalev.shopping.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.kovalev.shopping.BaseIntegrationTest;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.repository.CustomerRepository;
import ru.kovalev.shopping.repository.ProductRepository;

class ShoppingServiceImplTest extends BaseIntegrationTest {
    @Autowired
    ShoppingService shoppingService;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ProductRepository productRepository;

    @Test
    void addFirstItem() {
        var customer = new Customer();
        customer.setName("Name_buyer");
        customerRepository.save(customer);

        var product = new Product();
        product.setName("Something");
        product.setDescription("Something product description");
        var stored = 10;
        product.setStored(stored);
        productRepository.save(product);

        var quantity = 5;
        var buyerCart = shoppingService.getCustomersCart(customer);
        var cart = shoppingService.addItemToCart(buyerCart, product, quantity);

        assertThat(product.getReserved()).isEqualTo(stored - quantity);
        assertThat(cart.getItems()).hasSize(1)
                        .allMatch(i -> i.getProduct().equals(product) && i.getQuantity() == quantity);

        var p2 = productRepository.findById(product.getId());
        var cart2 = shoppingService.addItemToCart(cart, p2.get(), quantity);
        System.out.println("wow");
    }
}