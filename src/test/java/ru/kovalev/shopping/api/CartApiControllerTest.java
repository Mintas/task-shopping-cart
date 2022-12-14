package ru.kovalev.shopping.api;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.zalando.problem.DefaultProblem;
import ru.kovalev.shopping.BaseIntegrationTest;
import ru.kovalev.shopping.domain.CartState;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.exceptions.ConstraintViolationProblemWithDetail;
import ru.kovalev.shopping.rest.dto.CartResponseDto;
import ru.kovalev.shopping.rest.dto.ChangeQuantityRequest;
import ru.kovalev.shopping.rest.dto.ItemResponseDto;
import ru.kovalev.shopping.rest.dto.ProductResponseDto;
import ru.kovalev.shopping.rest.dto.RemoveItemRequest;
import ru.kovalev.shopping.validation.NotZero;

class CartApiControllerTest extends BaseIntegrationTest {
    public static final String CART_PATH = "/v0/cart/";

    private Product product;

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        this.product = productSomething10();
    }

    @Test
    void fetchCart() {
        var response =
                restTemplate.getForEntity(CART_PATH + "fetch", CartResponseDto.class);
        assertStatus(response, HttpStatus.OK);
        assertEmptyCart(response);
    }

    private void assertEmptyCart(ResponseEntity<CartResponseDto> response) {
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(cart -> {
                    assertThat(cart.getItems()).isEmpty();
                    assertThat(cart.getCartState()).isEqualTo(CartState.CART_IDLE.name());
                });
    }

    @Test
    void addItemToCart() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(product.getAvailableQuantity() - 1);

        var response =
                restTemplate.postForEntity(CART_PATH + "add", request, CartResponseDto.class);
        assertStatus(response, HttpStatus.OK);
        assertSingleItemCart(response, request, false, 1);
    }

    private void assertSingleItemCart(ResponseEntity<CartResponseDto> response, ChangeQuantityRequest request,
                                      boolean isDeletedItem, int productLeftovers) {
        assertSingleItemCart(response, request, isDeletedItem, productLeftovers, CartState.CART_IDLE);
    }

    private void assertSingleItemCart(ResponseEntity<CartResponseDto> response, ChangeQuantityRequest request,
                                      boolean isDeletedItem, int productLeftovers, CartState cartState) {
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(cart -> {
                    assertThat(cart.getItems())
                            .hasSize(1)
                            .first().satisfies(item -> assertItem(item, request, isDeletedItem, productLeftovers));
                    assertThat(cart.getCartState()).isEqualTo(cartState.name());
                });
    }

    private void assertItem(ItemResponseDto item, ChangeQuantityRequest request,
                            boolean isDeleted, int productLeftovers) {
        assertThat(item.getProduct())
                .extracting(ProductResponseDto::getId, ProductResponseDto::getName, ProductResponseDto::getDescription,
                        ProductResponseDto::getAvailableQuantity)
                .containsExactly(product.getId(), product.getName(), product.getDescription(), productLeftovers);
        assertThat(item.getQuantity()).isEqualTo(request.getQuantity());
        assertThat(item.getDeleted()).isEqualTo(isDeleted);
    }

    @Test
    void removeItemFromCart_throwsIfNotInCart() {
        var request = new RemoveItemRequest()
                .productId(product.getId());

        var response =
                restTemplate.postForEntity(CART_PATH + "remove", request, DefaultProblem.class);
        assertProductNotFound(response, request.getProductId(), " in cart");
    }

    @Test
    void removeItemFromCart_succeed() {
        var addItem = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(product.getAvailableQuantity() - 1);
        restTemplate.postForEntity(CART_PATH + "add", addItem, CartResponseDto.class);

        var request = new RemoveItemRequest()
                .productId(product.getId());
        var response =
                restTemplate.postForEntity(CART_PATH + "remove", request, CartResponseDto.class);
        assertStatus(response, HttpStatus.OK);
        assertSingleItemCart(response, addItem, true, product.getStored());
    }

    @Test
    void changeItemQuantity_throwsIfParameterConstraintViolated() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId());

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, DefaultProblem.class);
        assertConstraintVioldation(response, "quantity", "must not be null");
    }

    @Test
    void changeItemQuantity_throwsIfZeroQuantity() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(0);

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, DefaultProblem.class);
        assertConstraintVioldation(response, "changeItemQuantity.changeQuantityRequest.quantity", NotZero.NOT_ZERO_VIOLATION);
    }

    private void assertConstraintVioldation(ResponseEntity<DefaultProblem> response,
                                            String fieldName, String violation) {
        assertProblem(response, HttpStatus.BAD_REQUEST, ConstraintViolationProblemWithDetail.DETAIL_MESSAGE);
        assertThat(response.getBody()).extracting(DefaultProblem::getParameters)
                .extracting("violations").asList().hasSize(1)
                .first().isEqualTo(Map.of("field", fieldName, "message", violation));
    }

    @Test
    void changeItemQuantity_throwsIfInsufficientQuantity() {
        var quantity = product.getAvailableQuantity() + 1;
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(quantity);

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, DefaultProblem.class);
        assertProblem(response, HttpStatus.BAD_REQUEST,
                "Insufficient product quantity '%d' for '%s'. ".formatted(quantity, product.getId())
                        + "Stored: 10, Reserved: 0! Available: 10");
    }

    @Test
    void changeItemQuantity_throwsIfNotInCart() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(-product.getAvailableQuantity());

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, DefaultProblem.class);
        assertProductNotFound(response, request.getProductId(), " in cart");
    }

    @Test
    void changeItemQuantity_throwsIfUnknownProduct() {
        var request = new ChangeQuantityRequest()
                .productId(UUID.randomUUID())
                .quantity(25);

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, DefaultProblem.class);
        assertProductNotFound(response, request.getProductId(), "");
    }

    private void assertProductNotFound(ResponseEntity<DefaultProblem> response, UUID productId, String location) {
        assertProblem(response, HttpStatus.NOT_FOUND, a -> a.contains(
                "Entity of type 'Product' with id '%s' not found%s".formatted(productId, location)));
    }

    private void assertProblem(ResponseEntity<DefaultProblem> response, HttpStatus status, String details) {
        assertProblem(response, status, a -> a.isEqualTo(details));
    }

    private void assertProblem(ResponseEntity<DefaultProblem> response, HttpStatus status,
                               Consumer<AbstractStringAssert<?>> assertDetails) {
        assertStatus(response, status);
        assertThat(response.getBody())
                .satisfies(problem -> {
                    assertThat(problem.getTitle()).isEqualTo(status.getReasonPhrase());
                    assertDetails.accept(assertThat(problem.getDetail()));
                });
    }

    @Test
    void optimisticLockProtection_throws() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(product.getAvailableQuantity() - 2);

        restTemplate.getForEntity(CART_PATH + "fetch", CartResponseDto.class);
        var allCarts = cartRepository.findAll().iterator();
        var initCart = allCarts.next();
        assertThat(allCarts.hasNext()).isFalse();

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, ItemResponseDto.class);
        assertStatus(response, HttpStatus.OK);

        assertThatThrownBy(() -> shoppingService.updateQuantity(initCart, product, 2))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class)
                .hasMessageContainingAll(
                        "Object of class [ru.kovalev.shopping.domain.Product]",
                        "optimistic locking failed",
                        "Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)");
    }

    @Test
    void changeItemQuantity_succeed() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(product.getAvailableQuantity());

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, ItemResponseDto.class);
        assertStatus(response, HttpStatus.OK);
        assertItem(Objects.requireNonNull(response.getBody()),
                request, false, 0);

        var bigMinus = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(product.getAvailableQuantity() * (-1000));
        response = restTemplate.postForEntity(CART_PATH + "item/quantity", bigMinus, ItemResponseDto.class);
        assertStatus(response, HttpStatus.OK);
        assertItem(Objects.requireNonNull(response.getBody()),
                bigMinus.quantity(0), false, product.getStored());
    }

    @Test
    void orderCart() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(product.getAvailableQuantity());

        restTemplate.postForEntity(CART_PATH + "item/quantity", request, ItemResponseDto.class);

        var fetched = restTemplate.getForEntity(CART_PATH + "fetch", CartResponseDto.class);
        assertSingleItemCart(fetched, request, false, 0, CartState.CART_IDLE);

        var ordered = restTemplate.getForEntity(CART_PATH + "order", CartResponseDto.class);
        assertSingleItemCart(ordered, request, false, 0, CartState.CART_SUBMITTED);
    }

    @Test
    void orderCart_throwsOnEmptyCart() {
        var ordered = restTemplate.getForEntity(CART_PATH + "order", DefaultProblem.class);

        assertConstraintVioldation(ordered, "order.arg0.items", "must not be empty");
    }

    private void assertStatus(ResponseEntity<?> response, HttpStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
    }
}