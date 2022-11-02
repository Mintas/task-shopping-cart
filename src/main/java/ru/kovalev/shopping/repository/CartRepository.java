package ru.kovalev.shopping.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.kovalev.shopping.domain.Cart;

public interface CartRepository extends SoftDeleteRepository<Cart, UUID> {
    List<Cart> findAllByCustomerId(UUID customerId);

    Optional<Cart> findByIdAndDeletedIsFalse(UUID cartId);

    //there have to be at most one cart for user that is active ATM
    Optional<Cart> findFirstByCustomerIdAndDeletedIsFalse(UUID customerId);
}
