package ru.kovalev.shopping.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.kovalev.shopping.repository.CartRepository;
import ru.kovalev.shopping.service.CartExpirationService;
import ru.kovalev.shopping.service.CartExpirationServiceImpl;
import ru.kovalev.shopping.service.QuantityUpdateService;

@Configuration
@EnableScheduling
public class ExpirationConfig {
    @Bean
    Duration expirationRoutineDelay(
            @Value("${shopping.cart.expiration.routine-delay}") Duration delay) {
        return delay;
    }

    @Bean
    public CartExpirationService cartExpirationService(
            CartRepository cartRepository,
            QuantityUpdateService quantityUpdateService,
            @Value("${shopping.cart.expiration.ttl}") Duration expirationInterval) {
        return new CartExpirationServiceImpl(cartRepository, quantityUpdateService, expirationInterval);
    }
}
