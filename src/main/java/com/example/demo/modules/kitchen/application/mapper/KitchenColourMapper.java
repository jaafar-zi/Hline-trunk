package com.example.demo.modules.kitchen.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.kitchen.application.dto.response.KitchenColourResponse;
import com.example.demo.modules.kitchen.domain.model.KitchenColour;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kitchen Colour Mapper
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class KitchenColourMapper
        implements BaseMapper<KitchenColour, KitchenColourResponse> {

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "hexValue", source = "hexValue")
    @Mapping(target = "swatchImageUrl", source = "swatchImageUrl")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "sortOrder", source = "sortOrder")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    public abstract KitchenColourResponse toDto(KitchenColour colour);

    @Override
    public KitchenColour toEntity(KitchenColourResponse dto) {
        throw new UnsupportedOperationException("Use factory methods on KitchenColour");
    }

    @Override
    public List<KitchenColourResponse> toDtoList(List<KitchenColour> colours) {
        if (colours == null || colours.isEmpty()) return Collections.emptyList();
        return colours.stream().map(this::toDto).collect(Collectors.toList());
    }
}