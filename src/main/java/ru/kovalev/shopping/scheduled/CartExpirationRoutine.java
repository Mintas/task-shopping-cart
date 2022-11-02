package ru.kovalev.shopping.scheduled;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.kovalev.shopping.repository.CartRepository;
import ru.kovalev.shopping.service.CartExpirationService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartExpirationRoutine {
    private final CartExpirationService cartExpirationService;
    private final CartRepository cartRepository;

    @Scheduled(fixedDelayString = "#{expirationRoutineDelay.toMillis()}")
    public void expirationRoutine() {
        log.debug("Starting expiration routine at: {}", Instant.now());
        var activeCarts = cartRepository.findAllActive();
        var expired = 0;
        for (var cart : activeCarts) {
            var isExpired = cartExpirationService.expireCart(cart.getId());
            expired += isExpired ? 1 : 0;
        }
        log.debug("Expired {} obsolete carts. Finished at: {}", expired, Instant.now());
    }
}
