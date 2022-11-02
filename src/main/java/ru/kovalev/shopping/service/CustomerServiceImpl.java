package ru.kovalev.shopping.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CurrentUserName currentUserName;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer getCurrentCustomer() {
        var currentUser = currentUserName.get();
        var customer = customerRepository.findByNameAndDeletedIsFalse(currentUser);
        return customer.orElseGet(() -> initCustomer(currentUser));
    }

    private Customer initCustomer(String currentUser) {
        var customer = new Customer();
        customer.setName(currentUser);
        return customerRepository.save(customer);
    }
}
