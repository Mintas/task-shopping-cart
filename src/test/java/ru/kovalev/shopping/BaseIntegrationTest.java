package ru.kovalev.shopping;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.repository.CartRepository;
import ru.kovalev.shopping.repository.CustomerRepository;
import ru.kovalev.shopping.repository.ItemRepository;
import ru.kovalev.shopping.repository.ProductRepository;
import ru.kovalev.shopping.service.ShoppingService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
public class BaseIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:13.5"));

    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected CartRepository cartRepository;
    @Autowired
    protected ItemRepository itemRepository;
    @Autowired
    protected CustomerRepository customerRepository;
    @Autowired
    protected ShoppingService shoppingService;
    @Autowired
    protected TestRestTemplate restTemplate;

    @Value("${spring.security.user.name}")
    String basicName;
    @Value("${spring.security.user.password}")
    String basicPwd;

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("shopping.db.username", POSTGRES::getUsername);
        registry.add("shopping.db.password", POSTGRES::getPassword);
        registry.add("shopping.db.url", POSTGRES::getJdbcUrl);
    }

    @BeforeEach
    protected void setUp() {
        itemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        this.restTemplate = restTemplate.withBasicAuth("user", "user");
    }

    protected Customer createCustomer() {
        return createNamedCustomer("Name_buyer");
    }

    protected Customer createNamedCustomer(String name) {
        var customer = new Customer();
        customer.setName(name);
        return customerRepository.save(customer);
    }

    protected Product productSomething10() {
        var product = new Product();
        product.setName("Something");
        product.setDescription("Something product description");
        product.setStored(10);
        return productRepository.save(product);
    }
}
