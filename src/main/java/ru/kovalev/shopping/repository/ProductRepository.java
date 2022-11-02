package ru.kovalev.shopping.repository;

import java.util.UUID;
import ru.kovalev.shopping.domain.Product;

public interface ProductRepository extends SoftDeleteRepository<Product, UUID> {
}
