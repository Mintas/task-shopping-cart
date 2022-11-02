package ru.kovalev.shopping.mapper;

import org.mapstruct.Mapper;
import ru.kovalev.shopping.domain.Item;
import ru.kovalev.shopping.rest.dto.ItemResponseDto;

@Mapper
public interface ItemDtoMapper extends BaseDtoMapper<Item, ItemResponseDto> {
}
