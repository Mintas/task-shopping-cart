package ru.kovalev.shopping.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kovalev.shopping.config.SwaggerConfig;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.mapper.CartDtoMapper;
import ru.kovalev.shopping.mapper.ItemDtoMapper;
import ru.kovalev.shopping.rest.api.CartApi;
import ru.kovalev.shopping.rest.dto.CartResponseDto;
import ru.kovalev.shopping.rest.dto.ChangeQuantityRequest;
import ru.kovalev.shopping.rest.dto.ItemResponseDto;
import ru.kovalev.shopping.rest.dto.RemoveItemRequest;
import ru.kovalev.shopping.usecase.ShoppingCartUseCase;

@RestController
@Tag(name = "cart", description = "the cart API")
@RequestMapping("v0")
@SecurityRequirement(name = SwaggerConfig.SECURED)
@RequiredArgsConstructor
public class CartApiController implements CartApi {
    private final ShoppingCartUseCase shoppingCartUseCase;
    private final CartDtoMapper cartDtoMapper;
    private final ItemDtoMapper itemDtoMapper;

    @Override
    public ResponseEntity<CartResponseDto> fetchCart() {
        var cart = shoppingCartUseCase.fetchCart();
        return okCartReponse(cart);
    }

    @Override
    public ResponseEntity<CartResponseDto> addItemToCart(ChangeQuantityRequest addItemRequest) {
        var cartUpdated =
                shoppingCartUseCase.addItemToCart(addItemRequest.getProductId(), addItemRequest.getQuantity());
        return okCartReponse(cartUpdated);
    }

    @Override
    public ResponseEntity<CartResponseDto> removeItemFromCart(RemoveItemRequest removeItemRequest) {
        var cart = shoppingCartUseCase.removeItemFromCart(removeItemRequest.getProductId());
        return okCartReponse(cart);
    }

    @Override
    public ResponseEntity<ItemResponseDto> changeItemQuantity(ChangeQuantityRequest changeQuantityRequest) {
        var item = shoppingCartUseCase.changeItemQuantity(changeQuantityRequest.getProductId(),
                changeQuantityRequest.getQuantity());
        return ResponseEntity.ok(itemDtoMapper.toDto(item));
    }

    @Override
    public ResponseEntity<CartResponseDto> orderCart() {
        var cart = shoppingCartUseCase.orderCart();
        return okCartReponse(cart);
    }

    private ResponseEntity<CartResponseDto> okCartReponse(Cart cart) {
        return ResponseEntity.ok(cartDtoMapper.toDto(cart));
    }
}
