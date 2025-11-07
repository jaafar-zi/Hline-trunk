package com.example.demo.domain.userManagementDomain.address.service;

import com.example.demo.domain.userManagementDomain.address.dto.AddressDto;
import com.example.demo.domain.userManagementDomain.address.entity.Address;
import com.example.demo.domain.userManagementDomain.address.exception.AddressNotFoundException;
import com.example.demo.domain.userManagementDomain.address.exception.DuplicateDefaultAddressException;
import com.example.demo.domain.userManagementDomain.address.mapper.AddressMapper;
import com.example.demo.domain.userManagementDomain.address.repository.AddressRepository;
import com.example.demo.domain.userManagementDomain.user.entity.User;
import com.example.demo.domain.userManagementDomain.user.exception.UserNotFoundException;
import com.example.demo.domain.userManagementDomain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressMapper addressMapper;

    public AddressDto getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        return addressMapper.toDto(address);
    }

    public List<AddressDto> getAddressesByUserId(Integer userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(address -> this.addressMapper.toDto(address))
                .collect(Collectors.toList());
    }

    public AddressDto addAddress(AddressDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));

        if (dto.isDefault()) {
            List<Address> existingAddresses = addressRepository.findByUserId(user.getId());
            boolean hasDefault = existingAddresses.stream().anyMatch(Address::isDefault);
            if (hasDefault) {
                throw new DuplicateDefaultAddressException(user.getId().longValue());
            }
        }

        Address address = addressMapper.toEntity(dto);
        dto.setUserId(user.getId());
        address.setUser(user);
        addressRepository.save(address);
        return dto;
    }

    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException(id);
        }
        addressRepository.deleteById(id);
    }

    public AddressDto updateAddress(Long id, AddressDto dto) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));

        existing.setLabel(dto.getLabel());
        existing.setFullName(dto.getFullName());
        existing.setStreet(dto.getStreet());
        existing.setPostalCode(dto.getPostalCode());
        existing.setCity(dto.getCity());
        existing.setCountry(dto.getCountry());
        existing.setDefault(dto.isDefault());

        return addressMapper.toDto(addressRepository.save(existing));
    }
}

