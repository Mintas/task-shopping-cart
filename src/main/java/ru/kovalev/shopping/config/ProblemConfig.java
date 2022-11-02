package ru.kovalev.shopping.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.spring.web.autoconfigure.security.ProblemSecurityAutoConfiguration;

@Configuration
@EnableAutoConfiguration(exclude = {
        ErrorMvcAutoConfiguration.class,
        ProblemSecurityAutoConfiguration.class
})
public class ProblemConfig {
}
