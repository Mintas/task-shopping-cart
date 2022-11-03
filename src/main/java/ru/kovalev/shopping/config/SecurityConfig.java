package ru.kovalev.shopping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${shopping.security.oauth2:true}")
    private Boolean oidcEnabled = true;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         http.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and().logout();

        if (oidcEnabled) {
            http.oauth2Login(Customizer.withDefaults()).oauth2Client();
        } else {
            http.formLogin().and().httpBasic();
        }
        return http.build();
    }
}
