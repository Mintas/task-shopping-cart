package ru.kovalev.shopping.config;

import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.kovalev.shopping.validation.CartItemsValueExtractor;
import ru.kovalev.shopping.validation.ChangeQuantityValueExtractor;

@Configuration
public class ValidationConfig {

    @Bean
    public static LocalValidatorFactoryBean defaultValidator(ApplicationContext applicationContext) {
        var factoryBean = initValidatorWithMessageInterpolator(applicationContext);
        factoryBean.setConfigurationInitializer(cfg -> {
            cfg.addValueExtractor(new ChangeQuantityValueExtractor());
            cfg.addValueExtractor(new CartItemsValueExtractor());
        });
        factoryBean.afterPropertiesSet();
        return factoryBean;
    }

    /**
     * This method replicates original behaviour provided by spring autoconfigure:
     * <p>
     * {@link org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration#defaultValidator(ApplicationContext)}
     */
    private static LocalValidatorFactoryBean initValidatorWithMessageInterpolator(
            ApplicationContext applicationContext) {
        var factoryBean = new LocalValidatorFactoryBean();
        var interpolatorFactory = new MessageInterpolatorFactory(applicationContext);
        factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
        return factoryBean;
    }
}
