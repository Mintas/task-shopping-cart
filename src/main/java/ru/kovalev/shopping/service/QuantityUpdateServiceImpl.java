package ru.kovalev.shopping.service;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javax.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.exceptions.InsufficientQuantityProblem;
import ru.kovalev.shopping.exceptions.ShopEntityNotFoundProblem;
import ru.kovalev.shopping.repository.ItemRepository;
import ru.kovalev.shopping.repository.ProductRepository;

@Service("quantityUpdateService")
@RequiredArgsConstructor
public class QuantityUpdateServiceImpl implements QuantityUpdateService {
    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Item updateQuantity(Cart cart, Product product, @PositiveOrZero int quantity) {
        return updateQuantity(cart, product, quantity, UpdateMode.UPDATE);
    }

    @Override
    public Item removeItemFromCart(Cart cart, Product product) {
        var item = updateQuantity(cart, product, Integer.MIN_VALUE, UpdateMode.VERIFY);
        itemRepository.softDelete(item);
        return item;
    }

    public Item updateQuantity(Cart cart, Product product, int quantity, UpdateMode updateMode) {
        if (quantity == 0) {
            throw new IllegalArgumentException("cannot add 0 quantity");
        }
        if (quantity > 0) {
            return updateQuantity(cart, product,
                    it -> quantity,
                    this::storedEnoughToReserve,
                    o -> o.orElseGet(() -> addItem(cart, product, quantity)),
                    UpdateMode.UPDATE);
        } else {
            return updateQuantity(cart, product,
                    it -> Math.max(-it.getQuantity(), quantity),
                    this::canWithholdFromReserve,
                    o -> o.orElseThrow(() -> new ShopEntityNotFoundProblem(
                            Product.class, product.getId(), () -> "in cart " + cart.getId())),
                    updateMode);
        }
    }

    private Item addItem(Cart cart, Product product, int quantity) {
        var item = new Item();
        item.setProduct(product);
        item.setCart(cart);
        cart.getItems().add(item);
        return setItemQuantity(item, quantity);
    }

    private Optional<Item> selectItemFromCart(Cart cart, Product product) {
        return cart.getItems().stream()
                .filter(i -> i.getProduct().equals(product) && !i.isDeleted()).findFirst();
    }

    private Item updateQuantity(Cart cart, Product product,
                                Function<Item, Integer> limitQuantity,
                                BiPredicate<Integer, Product> limitCondition,
                                Function<Optional<Item>, Item> onNotFoundItem,
                                UpdateMode updateMode) {
        return onNotFoundItem.apply(
                selectItemFromCart(cart, product)
                        .map(item -> {
                            var quantity = limitQuantity.apply(item);
                            checkProductQuantityCondition(product, quantity, limitCondition);
                            return updateQuantity(item, product, quantity, updateMode);
                        }));
    }

    private void checkProductQuantityCondition(Product product, Integer quantity,
                                               BiPredicate<Integer, Product> limitCondition) {
        if (!limitCondition.test(quantity, product)) {
            throw new InsufficientQuantityProblem(product, quantity);
        }
    }

    public Item setItemQuantity(Item item, int quantity) {
        checkProductQuantityCondition(item.getProduct(), quantity, this::storedEnoughToReserve);
        return updateQuantity(item, item.getProduct(), quantity, UpdateMode.UPDATE);
    }

    private Item updateQuantity(Item item, Product product, int quantity, UpdateMode updateMode) {
        product.setReserved(product.getReserved() + quantity);
        productRepository.save(product);
        return updateItem(item, quantity, updateMode);
    }

    private Item updateItem(Item item, int quantity, UpdateMode updateMode) {
        if (UpdateMode.VERIFY == updateMode) {
            return itemRepository.findById(item.getId()).orElseThrow(() ->
                    new ShopEntityNotFoundProblem(Product.class, item.getProduct().getId()));
        }
        return itemRepository.save(item.addQuantity(quantity));
    }

    private boolean storedEnoughToReserve(int quantity, Product product) {
        return quantity >= 0 && product.getAvailableQuantity() - quantity >= 0;
    }

    private boolean canWithholdFromReserve(int quantity, Product product) {
        return quantity < 0 && product.getReserved() + quantity >= 0;
    }

    private enum UpdateMode {
        UPDATE, VERIFY
    }
}
