package com.example.demo.domain.userManagementDomain.address.repository;

import com.example.demo.domain.userManagementDomain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Integer userId);
}
