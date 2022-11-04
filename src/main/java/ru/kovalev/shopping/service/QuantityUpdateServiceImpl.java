package ru.kovalev.shopping.service;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javax.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.exceptions.ShopEntityNotFoundProblem;
import ru.kovalev.shopping.repository.ItemRepository;
import ru.kovalev.shopping.repository.ProductRepository;

@RequiredArgsConstructor
public class QuantityUpdateServiceImpl implements QuantityUpdateService {
    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final BiFunction<Item, Integer, Item> updateItem;

    public static QuantityUpdateService itemUpdating(ItemRepository itemRepository,
                                                     ProductRepository productRepository) {
        return new QuantityUpdateServiceImpl(itemRepository, productRepository,
                (item, quantity) -> itemRepository.save(item.addQuantity(quantity)));
    }

    public static QuantityUpdateService itemSkipping(ItemRepository itemRepository,
                                                     ProductRepository productRepository) {
        return new QuantityUpdateServiceImpl(itemRepository, productRepository,
                (item, quantity) -> itemRepository.findById(item.getId()).orElseThrow(IllegalAccessError::new));
    }

    @Override
    @Transactional
    public Item updateQuantity(Cart cart, Product product, @PositiveOrZero int quantity) {
        if (quantity == 0) {
            throw new IllegalArgumentException("cannot add 0 quantity");
        }
        if (quantity > 0) {
            return updateQuantity(cart, product,
                    it -> quantity,
                    this::storedEnoughToReserve,
                    o -> o.orElseGet(() -> addItem(cart, product, quantity)));
        } else {
            return updateQuantity(cart, product,
                    it -> Math.max(-it.getQuantity(), quantity),
                    this::canWithholdFromReserve,
                    o -> o.orElseThrow(() -> new ShopEntityNotFoundProblem(
                            Product.class, product.getId(), () -> "in cart " + cart.getId())));
        }
    }

    private Item addItem(Cart cart, Product product, int quantity) {
        var item = new Item();
        item.setProduct(product);
        item.setCart(cart);
        cart.getItems().add(item);
        return setItemQuantity(item, quantity);
    }

    private Optional<Item> findItem(Cart cart, Product product) {
        return cart.getItems().stream()
                .filter(i -> i.getProduct().equals(product) && !i.isDeleted()).findFirst();
    }

    private Item updateQuantity(Cart cart, Product product, Function<Item, Integer> limitQuantity,
                                BiPredicate<Integer, Product> limitCondition,
                                Function<Optional<Item>, Item> orElse) {
        return orElse.apply(
                findItem(cart, product)
                        .map(item -> {
                            var quantity = limitQuantity.apply(item);
                            checkProductQuantityCondition(product, quantity, limitCondition);
                            return updateQuantity(item, product, quantity);
                        }));
    }

    private void checkProductQuantityCondition(Product product, Integer quantity,
                                               BiPredicate<Integer, Product> limitCondition) {
        if (!limitCondition.test(quantity, product)) {
            throw new IllegalStateException(
                    "Insufficient item amount! Stored: %d, Reserved: %d! Available: %d"
                            .formatted(product.getStored(), product.getReserved(), product.getAvailableQuantity()));
        }
    }

    public Item setItemQuantity(Item item, int quantity) {
        checkProductQuantityCondition(item.getProduct(), quantity, this::storedEnoughToReserve);
        return updateQuantity(item, item.getProduct(), quantity);
    }

    private Item updateQuantity(Item item, Product product, int quantity) {
        product.setReserved(product.getReserved() + quantity);
        productRepository.save(product);
        return updateItem.apply(item, quantity);
    }

    private boolean storedEnoughToReserve(int quantity, Product product) {
        return quantity >= 0 && product.getAvailableQuantity() - quantity >= 0;
    }

    private boolean canWithholdFromReserve(int quantity, Product product) {
        return quantity < 0 && product.getReserved() + quantity >= 0;
    }
}
