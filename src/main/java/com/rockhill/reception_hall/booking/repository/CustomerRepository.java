package com.rockhill.reception_hall.booking.repository;

import com.rockhill.reception_hall.booking.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByEmail(String email);
}