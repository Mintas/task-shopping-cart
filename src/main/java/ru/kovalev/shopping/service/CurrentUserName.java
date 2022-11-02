package ru.kovalev.shopping.service;

import java.util.function.Supplier;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("currentUserName")
public class CurrentUserName implements Supplier<String> {
    public static final String SYSTEM = "System";

    @Override
    public String get() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) return SYSTEM;
        return authentication.getName();
    }
}