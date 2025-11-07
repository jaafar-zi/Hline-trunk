package com.example.demo.domain.userManagementDomain.address.mapper;

import com.example.demo.domain.userManagementDomain.address.dto.AddressDto;
import com.example.demo.domain.userManagementDomain.address.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "userId", source = "user.id")
    AddressDto toDto(Address address);

    @Mapping(target = "user", ignore = true)
    Address toEntity(AddressDto dto);

    List<AddressDto> toDtoList(List<Address> addresses);
}

