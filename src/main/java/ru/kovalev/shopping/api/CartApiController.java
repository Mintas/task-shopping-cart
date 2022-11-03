package ru.kovalev.shopping.api;

import java.util.UUID;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kovalev.shopping.config.SwaggerConfig;
import ru.kovalev.shopping.domain.Customer;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.mapper.CartDtoMapper;
import ru.kovalev.shopping.mapper.ItemDtoMapper;
import ru.kovalev.shopping.repository.ProductRepository;
import ru.kovalev.shopping.rest.api.CartApi;
import ru.kovalev.shopping.rest.dto.CartResponseDto;
import ru.kovalev.shopping.rest.dto.ChangeQuantityRequest;
import ru.kovalev.shopping.rest.dto.ItemResponseDto;
import ru.kovalev.shopping.rest.dto.RemoveItemRequest;
import ru.kovalev.shopping.service.CustomerService;
import ru.kovalev.shopping.service.ShoppingService;

@RestController
@Tag(name = "cart", description = "the cart API")
@RequestMapping("v0")
@SecurityRequirement(name = SwaggerConfig.SECURED)
@RequiredArgsConstructor
public class CartApiController implements CartApi {
    private final ShoppingService shoppingService;
    private final ProductRepository productRepository;
    private final CustomerService customerService;
    private final CartDtoMapper cartDtoMapper;
    private final ItemDtoMapper itemDtoMapper;

    @Override
    public ResponseEntity<CartResponseDto> fetchCart() {
        var cart = doFetchCart();
        return cartToDto(cart);
    }

    @Override
    public ResponseEntity<CartResponseDto> addItemToCart(ChangeQuantityRequest addItemRequest) {
        var product = fetchProduct(addItemRequest.getProductId());
        var cart = doFetchCart();

        var cartUpdated = shoppingService.addItemToCart(cart, product, addItemRequest.getQuantity());
        return cartToDto(cartUpdated);
    }

    @Override
    public ResponseEntity<CartResponseDto> removeItemFromCart(RemoveItemRequest removeItemRequest) {
        var product = fetchProduct(removeItemRequest.getProductId());
        var cart = doFetchCart();

        var yes = shoppingService.removeItemFromCart(cart, product);
        return cartToDto(cart);
    }

    @Override
    public ResponseEntity<ItemResponseDto> changeItemQuantity(ChangeQuantityRequest changeQuantityRequest) {
        var product = fetchProduct(changeQuantityRequest.getProductId());
        var cart = doFetchCart();

        var item = shoppingService.updateQuantity(cart, product, changeQuantityRequest.getQuantity());
        return itemToDto(item);
    }

    @Override
    public ResponseEntity<CartResponseDto> orderCart() {
        var cart = doFetchCart();
        //todo : throw exception if cart is empty!
        shoppingService.order(cart);
        return cartToDto(cart);
    }

    private ResponseEntity<ItemResponseDto> itemToDto(Item item) {
        return ResponseEntity.ok(itemDtoMapper.toDto(item));
    }

    private Cart doFetchCart() {
        var customer = fetchCustomer();
        return shoppingService.getCustomersCart(customer);
    }

    private ResponseEntity<CartResponseDto> cartToDto(Cart cart) {
        return ResponseEntity.ok(cartDtoMapper.toDto(cart));
    }

    private Product fetchProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("product not identified"));
    }

    private Customer fetchCustomer() {
        return customerService.getCurrentCustomer();
    }
}
