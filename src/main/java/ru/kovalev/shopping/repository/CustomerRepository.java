package ru.kovalev.shopping.repository;

import java.util.Optional;
import java.util.UUID;
import ru.kovalev.shopping.domain.Customer;

public interface CustomerRepository extends SoftDeleteRepository<Customer, UUID> {
    Optional<Customer> findByNameAndDeletedIsFalse(String name);
}
