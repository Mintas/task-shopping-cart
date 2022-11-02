package ru.kovalev.shopping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kovalev.shopping.repository.ItemRepository;
import ru.kovalev.shopping.repository.ProductRepository;
import ru.kovalev.shopping.service.QuantityUpdateService;
import ru.kovalev.shopping.service.QuantityUpdateServiceImpl;

@Configuration
public class ServiceConfig {
    @Bean
    public QuantityUpdateService itemUpdatingService(ItemRepository itemRepository,
                                                     ProductRepository productRepository) {
        return QuantityUpdateServiceImpl.itemUpdating(itemRepository, productRepository);
    }

    @Bean
    public QuantityUpdateService itemSkippingService(ItemRepository itemRepository,
                                                     ProductRepository productRepository) {
        return QuantityUpdateServiceImpl.itemSkipping(itemRepository, productRepository);
    }
}
