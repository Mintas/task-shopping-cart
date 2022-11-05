/*
 * Copyright (c) 2021 Sberbank. All rights reserved.
 */

package ru.kovalev.shopping.mapper;

import org.mapstruct.MapperConfig;
import ru.kovalev.shopping.domain.BaseEntity;

@MapperConfig
public interface ToDtoMapper<E extends BaseEntity, DTO> {

    DTO toDto(E entity);
}
