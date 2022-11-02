/*
 * Copyright (c) 2021 Sberbank. All rights reserved.
 */

package ru.kovalev.shopping.mapper;

import java.util.List;
import org.mapstruct.MapperConfig;
import ru.kovalev.shopping.domain.BaseEntity;

@MapperConfig
public interface BaseDtoMapper<E extends BaseEntity, DTO> {

    DTO toDto(E entity);

    List<DTO> toList(List<E> entities);

//    E initFor(CREATE data);
//
//    void apply(@MappingTarget E entity, ADJUSTMENT adjustment);

//    default UUID mapRegion(Region region) {
//        return region.getId();
//    }
}
