package com.example.demo.modules.order.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Address Value Object
 * 
 * Represents a shipping or billing address
 * 
 * @author Order Team
 * @version 1.0
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address implements Serializable {

    @Column(name = "street_address", nullable = false, length = 255)
    private String streetAddress;

    @Column(name = "street_address_2", length = 255)
    private String streetAddress2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state_province", length = 100)
    private String stateProvince;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 2)
    private String country; // ISO 3166-1 alpha-2 code

    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Validate address
     */
    public void validate() {
        if (streetAddress == null || streetAddress.isBlank()) {
            throw new IllegalArgumentException("Street address is required");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code is required");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }
        if (country.length() != 2) {
            throw new IllegalArgumentException("Country must be ISO 3166-1 alpha-2 code");
        }
    }

    /**
     * Get full address as string
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(streetAddress);
        if (streetAddress2 != null && !streetAddress2.isBlank()) {
            sb.append(", ").append(streetAddress2);
        }
        sb.append(", ").append(city);
        if (stateProvince != null && !stateProvince.isBlank()) {
            sb.append(", ").append(stateProvince);
        }
        sb.append(" ").append(postalCode);
        sb.append(", ").append(country);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address address = (Address) o;
        return Objects.equals(streetAddress, address.streetAddress) &&
               Objects.equals(streetAddress2, address.streetAddress2) &&
               Objects.equals(city, address.city) &&
               Objects.equals(stateProvince, address.stateProvince) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streetAddress, streetAddress2, city, stateProvince, postalCode, country);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}