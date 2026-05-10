package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByDeletedFalse();

    Optional<Customer> findByIdAndDeletedFalse(UUID id);

    // Used by ProjectMapper to resolve customer name for soft-deleted entities
    // (FR-CUS-008)
    // Intentionally no deleted filter — inherited from JpaRepository.findById

    boolean existsByCustomerCodeIgnoreCaseAndDeletedFalse(String customerCode);

    Optional<Customer> findByCustomerCodeIgnoreCaseAndDeletedFalse(String customerCode);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.customer.id = :customerId AND p.deleted = false")
    long countActiveProjectsByCustomerId(UUID customerId);
}
