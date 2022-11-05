package ru.kovalev.shopping.cdc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.debezium.testing.testcontainers.Connector;
import io.debezium.testing.testcontainers.ConnectorConfiguration;
import io.debezium.testing.testcontainers.DebeziumContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.shaded.org.awaitility.Durations;
import org.testcontainers.utility.DockerImageName;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.mapper.CartDtoMapper;
import ru.kovalev.shopping.repository.CartRepository;
import ru.kovalev.shopping.repository.ProductRepository;
import ru.kovalev.shopping.service.CustomerService;
import ru.kovalev.shopping.service.ShoppingService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
@Slf4j
public class DebeziumConnectorTest {
    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:13.5"))
                    .withNetwork(NETWORK)
                    .withNetworkAliases("postgres")
                    .withCommand("postgres -c wal_level=logical");
    @Container
    protected static final KafkaContainer KAFKA =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
                    .withNetwork(NETWORK);

    private static final Slf4jLogConsumer DEBEZIUM_LOG_CONSUMER = new Slf4jLogConsumer(log);
    @Container
    protected static final DebeziumContainer DEBEZIUM =
            new DebeziumContainer("debezium/connect:1.9.4.Final")
                    .withNetwork(NETWORK)
                    .withKafka(KAFKA)
                    .dependsOn(KAFKA, POSTGRES)
                    .withLogConsumer(DEBEZIUM_LOG_CONSUMER);

    @Autowired
    ProductRepository productRepository;
    @Autowired
    ShoppingService shoppingService;
    @Autowired
    CustomerService customerService;
    @Autowired
    CartDtoMapper cartDtoMapper;
    @Autowired
    CartRepository cartRepository;

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("shopping.db.username", POSTGRES::getUsername);
        registry.add("shopping.db.password", POSTGRES::getPassword);
        registry.add("shopping.db.url", POSTGRES::getJdbcUrl);
    }

    @Test
    public void debezium_should_captureCartUpdates() {
        registerDebeziumConnector();

        var systemCustomer = customerService.getCurrentCustomer();

        var product = new Product();
        product.setStored(10);
        product.setName("story");
        product.setDescription("of a boy");
        var saved = productRepository.save(product);

        var cart = shoppingService.getCustomersCart(systemCustomer);
        shoppingService.addItemToCart(cart, saved, 5);
        shoppingService.order(cart);

        var consumer = cdcConsumer();
        var messages = drainMessages(consumer);
        System.out.println(messages);
        var msg = messages.get(0);
        assertThat(msg.value())
                .usingRecursiveComparison()
                .ignoringFields("customer", "items")
                .isEqualTo(cartRepository.findById(cart.getId()).get());
    }

    private void registerDebeziumConnector() {
        var connector = Connector.fromJson(this.getClass().getResourceAsStream("/debezium-pg-conn.json"));
        connector.appendOrOverrideConfiguration(ConnectorConfiguration.forJdbcContainer(POSTGRES));
        var configuration = extractFieldValueViaReflection(connector);
        DEBEZIUM.registerConnector("cart-orders", configuration);
    }

    private KafkaConsumer<?, ?> cdcConsumer() {
        var cartMapper = JacksonUtils.enhancedObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        var consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                new JsonDeserializer<>(new TypeReference<Map<String, UUID>>() {
                }),
                new JsonDeserializer<>(Cart.class, cartMapper));
        //consumer.subscribe(List.of("shoppingdb.public.cart"));
        consumer.subscribe(List.of("CART_SUBMITTED"));
        return consumer;
    }

    private List<ConsumerRecord<?, ?>> drainMessages(KafkaConsumer<?, ?> consumer) {
        List<ConsumerRecord<?, ?>> messages = new ArrayList<>();
        Awaitility.await()
                .atMost(Durations.TEN_SECONDS)
                .pollInterval(Durations.ONE_HUNDRED_MILLISECONDS)
                .until(() -> {
                    var poll = consumer.poll(Duration.ofMillis(50));
                    poll.forEach(messages::add);
                    return messages.size() == 1;
                });
        return messages;
    }

    @SneakyThrows
    private ConnectorConfiguration extractFieldValueViaReflection(Connector connector) {
        var cField = Connector.class.getDeclaredField("configuration");
        cField.setAccessible(true);
        return (ConnectorConfiguration) cField.get(connector);
    }
}
