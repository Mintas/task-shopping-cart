package ru.kovalev.shopping.service;

import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.exceptions.InsufficientQuantityProblem;
import ru.kovalev.shopping.exceptions.ShopEntityNotFoundProblem;
import ru.kovalev.shopping.repository.ItemRepository;
import ru.kovalev.shopping.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class QuantityUpdateServiceImplTest {
    @InjectMocks
    private QuantityUpdateServiceImpl quantityUpdateService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ProductRepository productRepository;
    private final int stored = 10;
    private final int reservedInitially = 1;
    private final int itemQuantity = 5;
    private Product product;
    private Item item;
    private Cart cart;

    @Nested
    @DisplayName("withItemPresent")
    class WithItemPresentTests {
        @BeforeEach
        void setUp() {
            product = createProduct();
            item = createItem(product, itemQuantity);
            cart = createCart(item);
        }

        @Test
        void updateQuantity_throwsOnZero() {
            assertThatThrownBy(() -> quantityUpdateService.updateQuantity(cart, product, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cannot add 0 quantity");
        }

        @Test
        void updateQuantity_positiveNotEnough() {
            var quantityUpdate = product.getAvailableQuantity() + 1;
            assertThatThrownBy(() -> quantityUpdateService.updateQuantity(cart, product, quantityUpdate))
                    .isInstanceOf(InsufficientQuantityProblem.class)
                    .hasMessageContainingAll("Bad Request",
                            InsufficientQuantityProblem.formatMessage(product, quantityUpdate));
        }

        @Test
        void updateQuantity_negativeTooMuch() {
            when(productRepository.save(product)).thenReturn(product);
            when(itemRepository.save(item)).thenReturn(item);

            var quantityUpdate = -(product.getReserved() + 1);
            var updated = quantityUpdateService.updateQuantity(cart, product, quantityUpdate);
            assertUpdated(updated, 0);
        }

        @ValueSource(ints = {4, -5})
        @ParameterizedTest
        void updateQuantity_succeed(int quantityUpdate) {
            when(productRepository.save(product)).thenReturn(product);
            when(itemRepository.save(item)).thenReturn(item);

            var updated = quantityUpdateService.updateQuantity(cart, product, quantityUpdate);
            assertUpdated(updated, itemQuantity + quantityUpdate);
        }

        @Test
        void removeItemFromCart() {
            when(productRepository.save(product)).thenReturn(product);
            when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
            doAnswer(AdditionalAnswers.<Item>answerVoid((item -> item.setDeleted(true))))
                    .when(itemRepository).softDelete(item);

            var updated = quantityUpdateService.removeItemFromCart(cart, product);
            assertUpdated(updated, itemQuantity, true, reservedInitially);
        }

        @Test
        void removeItemFromCart_failsIfInCartButNotInDB() {
            when(productRepository.save(product)).thenReturn(product);
            when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> quantityUpdateService.removeItemFromCart(cart, product))
                    .isInstanceOf(ShopEntityNotFoundProblem.class)
                    .hasMessageContaining(product.getId().toString(), "not found");
        }
    }

    @Nested
    @DisplayName("withItemAbsent")
    class WithItemAbsentTests {
        @BeforeEach
        void setUp() {
            product = createProduct();
            item = createItem(product, itemQuantity);
            cart = createCart();
        }

        @Test
        void updateQuantity_positiveNotEnough() {
            doTestUpdateQuantity_positiveNotEnough();
        }

        @ValueSource(ints = {-5, -stored})
        @ParameterizedTest
        void updateQuantity_negativeTooMuch(int negativeQuantity) {
            assertThatThrownBy(() -> quantityUpdateService.updateQuantity(cart, product, negativeQuantity))
                    .isInstanceOf(ShopEntityNotFoundProblem.class)
                    .hasMessageContainingAll("Not Found",
                            ShopEntityNotFoundProblem.formatMessage(Product.class, product.getId(),
                                    () -> "in cart " + cart.getId()));
        }

        @Test
        void updateQuantity_succeed() {
            var reserved = product.getReserved();
            assertThat(reserved).isEqualTo(reservedInitially + item.getQuantity());

            when(productRepository.save(product)).thenReturn(product);
            when(itemRepository.save(any(Item.class))).thenAnswer((invoc) -> invoc.getArgument(0));

            var quantityUpdate = stored - reserved;
            var updated = quantityUpdateService.updateQuantity(cart, product, quantityUpdate);
            assertThat(updated).isNotSameAs(item)
                    .isNotEqualTo(item)
                    .extracting(Item::getQuantity, Item::isDeleted)
                    .containsExactly(quantityUpdate, false);
            assertProductUpdate(updated, reserved + quantityUpdate);
        }

        @Test
        void removeItemFromCart() {
            assertThatThrownBy(() -> quantityUpdateService.removeItemFromCart(cart, product))
                    .isInstanceOf(ShopEntityNotFoundProblem.class)
                    .hasMessageContainingAll("Not Found",
                            ShopEntityNotFoundProblem.formatMessage(Product.class, product.getId(),
                                    () -> "in cart " + cart.getId()));
        }
    }

    private void assertProductUpdate(Item updated, int reservedAfterUpdate) {
        assertThat(updated.getProduct())
                .satisfies(p -> assertThat(p).isSameAs(product)
                        .extracting(Product::getStored, Product::getReserved)
                        .containsExactly(stored, reservedAfterUpdate));
    }

    private void doTestUpdateQuantity_positiveNotEnough() {
        var quantityUpdate = product.getAvailableQuantity() + 1;
        assertThatThrownBy(() -> quantityUpdateService.updateQuantity(cart, product, quantityUpdate))
                .isInstanceOf(InsufficientQuantityProblem.class)
                .hasMessageContainingAll("Bad Request",
                        InsufficientQuantityProblem.formatMessage(product, quantityUpdate));
    }

    private void assertUpdated(Item updated, int newQuantity) {
        assertUpdated(updated, newQuantity, false, newQuantity + reservedInitially);
    }

    private void assertUpdated(Item updated, int newQuantity, boolean softDeleted, int reserved) {
        assertThat(updated).isSameAs(item)
                .extracting(Item::getQuantity, Item::isDeleted)
                .containsExactly(newQuantity, softDeleted);
        assertProductUpdate(updated, reserved);
    }

    private Item createItem(Product product, int itemQuantity) {
        var item = new Item();
        item.setId(UUID.randomUUID());
        item.setProduct(product);
        item.setQuantity(itemQuantity);
        product.setReserved(product.getReserved() + itemQuantity);
        return item;
    }

    private Product createProduct() {
        var product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("namee");
        product.setDescription("descriptee");
        product.setStored(stored);
        product.setReserved(reservedInitially);
        return product;
    }

    private Cart createCart() {
        var cart = new Cart();
        cart.setId(UUID.randomUUID());
        return cart;
    }

    private Cart createCart(Item item) {
        var cart = createCart();
        cart.getItems().add(item);
        return cart;
    }
}