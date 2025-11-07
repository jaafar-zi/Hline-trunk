package com.example.demo.domain.userManagementDomain.address.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {
    private Long id;
    private String label;
    private String fullName;
    private String street;
    private String postalCode;
    private String city;
    private String country;
    private boolean isDefault;
    private Integer userId;
}
