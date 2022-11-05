package ru.kovalev.shopping.mapper;

import org.mapstruct.Mapper;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.rest.dto.CartResponseDto;

@Mapper
public interface CartDtoMapper extends ToDtoMapper<Cart, CartResponseDto> {
}
