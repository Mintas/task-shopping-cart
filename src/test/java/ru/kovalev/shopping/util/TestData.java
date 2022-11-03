package ru.kovalev.shopping.util;

import ru.kovalev.shopping.domain.Product;

public class TestData {
    public static Product productSomething10() {
        var product = new Product();
        product.setName("Something");
        product.setDescription("Something product description");
        product.setStored(10);
        return product;
    }
}
