package ru.kovalev.shopping.service;

import java.util.UUID;

public interface CartExpirationService {
    boolean expireCart(UUID cartId);
}
