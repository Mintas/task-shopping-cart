package ru.kovalev.shopping.api;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import static java.util.Collections.nCopies;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.kovalev.shopping.api.CartApiControllerTest.CART_PATH;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.zalando.problem.DefaultProblem;
import ru.kovalev.shopping.BaseIntegrationTest;
import ru.kovalev.shopping.rest.dto.CartResponseDto;
import ru.kovalev.shopping.rest.dto.ChangeQuantityRequest;

public class CartApiMultiClientBehaviourTest extends BaseIntegrationTest {
    @Test
    void addItemToCart_multithreaded() throws InterruptedException {
        var stored = 100;
        var product = createProduct("bench", stored);
        product.setStored(stored);
        productRepository.save(product);

        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(1);

        var executors = Runtime.getRuntime().availableProcessors();
        var threadPool = Executors.newFixedThreadPool(executors);
        var workShare = stored / executors;
        var errorCounter = new LongAdder();
        var takeOne = (Callable<Boolean>) () -> {
            for (var i = 0; i < workShare; i++) {
                var response =
                        restTemplate.postForEntity(CART_PATH + "add", request, DefaultProblem.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    errorCounter.increment();
                }
            }
            return true;
        };

        try {
            threadPool.invokeAll(nCopies(executors, takeOne));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        }

        var response =
                restTemplate.getForEntity(CART_PATH + "fetch", CartResponseDto.class);
        assertThat(response.getStatusCode()).isSameAs(HttpStatus.OK);

        var takenItem = workShare * executors - errorCounter.intValue();

        assertThat(response.getBody()).extracting(CartResponseDto::getItems)
                .asList().hasSize(1)
                .first()
                .extracting("quantity", "product.availableQuantity")
                .containsExactly(takenItem, stored - takenItem);
    }
}
