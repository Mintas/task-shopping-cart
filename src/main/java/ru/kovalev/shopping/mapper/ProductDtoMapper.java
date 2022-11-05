package ru.kovalev.shopping.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kovalev.shopping.domain.Product;
import ru.kovalev.shopping.rest.dto.CreateBookRequest;
import ru.kovalev.shopping.rest.dto.ProductListDto;
import ru.kovalev.shopping.rest.dto.ProductResponseDto;

@Mapper
public interface ProductDtoMapper extends BaseDtoMapper<Product, ProductResponseDto> {

    @Mapping(target = "stored", source = "quantity")
    Product toEntity(CreateBookRequest request);

    default ProductListDto toListDto(Iterable<Product> entities) {
        var result = new ProductListDto();
        result.books(toList(entities));
        return result;
    }
}
