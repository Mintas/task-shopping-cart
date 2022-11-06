package ru.kovalev.shopping.config;

import java.util.function.Consumer;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.kovalev.shopping.validation.CartItemsValueExtractor;
import ru.kovalev.shopping.validation.ChangeQuantityValueExtractor;

@Configuration
public class ValidationConfig {

    /**
     * @see <a href="https://github.com/spring-projects/spring-boot/pull/29429">Autoconfiguration behaviour is included in Spring Boot 3.0</a>
     *
     */
    @Bean
    public LocalValidatorFactoryBean defaultValidator(ApplicationContext applicationContext) {
        var factoryBean = ValidationAutoConfiguration.defaultValidator(applicationContext);
        factoryBean.setConfigurationInitializer(configurationInitializer());
        return factoryBean;
    }

    private Consumer<javax.validation.Configuration<?>> configurationInitializer() {
        return cfg -> {
            cfg.addValueExtractor(new ChangeQuantityValueExtractor());
            cfg.addValueExtractor(new CartItemsValueExtractor());
        };
    }
}
