package com.example.demo.domain.userManagementDomain.address.controller;

import com.example.demo.domain.userManagementDomain.address.dto.AddressDto;
import com.example.demo.domain.userManagementDomain.address.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/{Id}")
    public ResponseEntity<AddressDto> getAddressById( @PathVariable String Id) {
        return ResponseEntity.ok(addressService.getAddressById(Long.parseLong(Id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDto>> getAddresses(@PathVariable Integer userId) {
        return ResponseEntity.ok(addressService.getAddressesByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(@RequestBody AddressDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.addAddress(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Long id, @RequestBody AddressDto dto) {
        return ResponseEntity.ok(addressService.updateAddress(id, dto));
    }
}

