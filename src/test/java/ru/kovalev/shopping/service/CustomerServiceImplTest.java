package ru.kovalev.shopping.service;

import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @InjectMocks
    private CustomerServiceImpl customerService;
    @Mock
    private CurrentUserName currentUserName;
    @Mock
    private CustomerRepository customerRepository;
    private String name = "cutomer";
    private Customer customer = createCustomer();

    @BeforeEach
    void setUp() {
        when(currentUserName.get()).thenReturn(name);
    }

    @Test
    void getCurrentCustomer_ifPresent() {
        when(customerRepository.findByNameAndDeletedIsFalse(name))
                .thenReturn(Optional.of(customer));

        var currentCustomer = customerService.getCurrentCustomer();
        assertThat(currentCustomer).isSameAs(customer).isEqualTo(customer);
    }

    @Test
    void getCurrentCustomer_ifAbsent() {
        when(customerRepository.findByNameAndDeletedIsFalse(name))
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer((inv) -> inv.getArgument(0));

        var currentCustomer = customerService.getCurrentCustomer();
        assertThat(currentCustomer).isNotEqualTo(customer);
        assertThat(currentCustomer.getName()).isEqualTo(name);
    }


    private Customer createCustomer() {
        var customer = new Customer();
        customer.setName(name);
        customer.setId(UUID.randomUUID());
        return customer;
    }
}