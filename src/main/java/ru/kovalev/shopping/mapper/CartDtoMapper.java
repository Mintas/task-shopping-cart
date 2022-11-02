package ru.kovalev.shopping.mapper;

import org.mapstruct.Mapper;
import ru.kovalev.shopping.domain.Cart;
import ru.kovalev.shopping.rest.dto.CartResponseDto;

@Mapper
public interface CartDtoMapper extends BaseDtoMapper<Cart, CartResponseDto> {

//    @Mapping(target = "createdAt", )
//    Cart fromSqlMap(Map<String, Object> asMapWithSqlNamedFields);
}
