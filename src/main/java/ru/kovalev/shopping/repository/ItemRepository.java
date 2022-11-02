package ru.kovalev.shopping.repository;

import java.util.UUID;
import ru.kovalev.shopping.domain.Item;

public interface ItemRepository extends SoftDeleteRepository<Item, UUID> {
}
