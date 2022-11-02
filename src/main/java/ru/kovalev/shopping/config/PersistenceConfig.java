package ru.kovalev.shopping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.kovalev.shopping.repository.SoftDeleteRepositoryImpl;

@Configuration
@EnableJpaRepositories(basePackages = "ru.kovalev.shopping.repository",
        repositoryBaseClass = SoftDeleteRepositoryImpl.class)
public class PersistenceConfig {

}
