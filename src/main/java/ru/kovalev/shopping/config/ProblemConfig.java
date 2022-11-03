package ru.kovalev.shopping.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;
import org.zalando.problem.spring.web.autoconfigure.security.ProblemSecurityAutoConfiguration;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@Configuration
@EnableAutoConfiguration(exclude = {
        ErrorMvcAutoConfiguration.class,
        ProblemSecurityAutoConfiguration.class,
        ProblemJacksonAutoConfiguration.class
})
public class ProblemConfig {

    @Bean
    public ProblemModule problemModule() {
        return new ProblemModule().withStackTraces(false);
    }

    @Bean
    public ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }

}
