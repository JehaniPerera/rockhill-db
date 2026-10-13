package com.rockhill.reception_hall.booking.repository;

import com.rockhill.reception_hall.booking.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByPhone(String phone);
    Optional<Customer> findFirstByEmail(String email);
}