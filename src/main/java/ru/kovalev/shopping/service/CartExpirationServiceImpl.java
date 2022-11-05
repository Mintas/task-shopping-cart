package ru.kovalev.shopping.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BinaryOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalev.shopping.domain.BaseEntity;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.CartState;
import ru.kovalev.shopping.repository.CartRepository;

@RequiredArgsConstructor
public class CartExpirationServiceImpl implements CartExpirationService {
    private final CartRepository cartRepository;
    private final QuantityUpdateService quantityUpdateService;
    private final Duration expirationInterval;

    @Override
    @Transactional
    public boolean expireCart(UUID cartId) {
        var cart = cartRepository.findByIdAndDeletedIsFalse(cartId);
        return cart.map(this::checkAndExpire)
                .orElse(false);
    }

    private boolean checkAndExpire(Cart cart) {
        if (isExpired(cart)) {
            doExpire(cart);
            return true;
        }
        return false;
    }

    private boolean isExpired(Cart cart) {
        var lastUpdateTime = getLastUpdateTime(cart);
        var activeCartTime = Duration.between(lastUpdateTime, Instant.now());
        return activeCartTime.compareTo(expirationInterval) >= 0;
    }

    private Instant getLastUpdateTime(Cart cart) {
        return cart.getItems().stream()
                .map(BaseEntity::getUpdatedAt)
                .reduce(cart.getUpdatedAt(), BinaryOperator.maxBy(Instant::compareTo));
    }

    private void doExpire(Cart cart) {
        cart.setCartState(CartState.CART_OBSOLETE);
        cartRepository.softDelete(cart);
        cart.getItems().forEach(item ->
                quantityUpdateService.updateQuantity(cart, item.getProduct(), -item.getQuantity()));
    }
}
