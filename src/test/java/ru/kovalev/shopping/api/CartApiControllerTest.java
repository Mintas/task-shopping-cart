package ru.kovalev.shopping.api;

import java.util.Map;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Status;
import ru.kovalev.shopping.BaseIntegrationTest;
import ru.kovalev.shopping.domain.CartState;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.rest.dto.CartResponseDto;
import ru.kovalev.shopping.rest.dto.ChangeQuantityRequest;
import ru.kovalev.shopping.rest.dto.ItemResponseDto;
import ru.kovalev.shopping.rest.dto.RemoveItemRequest;
import ru.kovalev.shopping.util.TestData;

class CartApiControllerTest extends BaseIntegrationTest {
    public static final String CART_PATH = "/v0/cart/";

    private Product product;

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();
        var product = TestData.productSomething10();
        this.product = productRepository.save(product);
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
                .extracting("id", "name", "description", "availableQuantity")
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
        assertStatus(response, HttpStatus.INTERNAL_SERVER_ERROR);
        assertNotInCartProblem(response);
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
        var request = new RemoveItemRequest()
                .productId(product.getId());

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, DefaultProblem.class);
        assertStatus(response, HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .satisfies(problem -> {
                    assertThat(problem.getTitle()).isEqualTo(Status.BAD_REQUEST.getReasonPhrase());
                    assertThat(problem.getDetail()).isEqualTo("Constraint Violation");
                    assertThat(problem.getParameters().get("violations"))
                            .asList().hasSize(1).first()
                            .isEqualTo(Map.of("field", "quantity", "message", "не должно равняться null"));
                });
    }

    @Test
    void changeItemQuantity_throwsIfInsufficientQuantity() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(product.getAvailableQuantity() + 1);

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, DefaultProblem.class);
        assertStatus(response, HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .satisfies(problem -> {
                    assertThat(problem.getTitle()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                    assertThat(problem.getDetail()).isEqualTo(
                            "Insufficient item amount! Stored: 10, Reserved: 0! Available: 10");
                });
    }

    @Test
    void changeItemQuantity_throwsIfNotInCart() {
        var request = new ChangeQuantityRequest()
                .productId(product.getId())
                .quantity(-product.getAvailableQuantity());

        var response =
                restTemplate.postForEntity(CART_PATH + "item/quantity", request, DefaultProblem.class);
        assertStatus(response, HttpStatus.INTERNAL_SERVER_ERROR);
        assertNotInCartProblem(response);
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

    private void assertNotInCartProblem(ResponseEntity<DefaultProblem> response) {
        assertThat(response.getBody())
                .satisfies(problem -> {
                    assertThat(problem.getTitle()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                    assertThat(problem.getDetail()).isEqualTo("no such product in cart");
                });
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

    private void assertStatus(ResponseEntity<?> response, HttpStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
    }
}