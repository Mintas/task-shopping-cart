/*
 * Copyright (c) 2021 Sberbank. All rights reserved.
 */

package ru.kovalev.shopping.mapper;

import java.util.List;
import org.mapstruct.MapperConfig;
import ru.kovalev.shopping.domain.BaseEntity;

@MapperConfig
public interface BaseDtoMapper<E extends BaseEntity, DTO> extends ToDtoMapper<E, DTO> {

    List<DTO> toList(Iterable<E> entities);
}
