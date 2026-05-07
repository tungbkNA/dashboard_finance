package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.customer.CustomerRequest;
import com.internal.projectmgmt.dto.customer.CustomerResponse;
import com.internal.projectmgmt.entity.Customer;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.CustomerMapper;
import com.internal.projectmgmt.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerResponse> findAll() {
        return customerRepository.findAllByDeletedFalse().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByCustomerCodeIgnoreCaseAndDeletedFalse(request.customerCode())) {
            throw new AppException("CUSTOMER_CODE_DUPLICATE", "Mã khách hàng đã tồn tại");
        }
        Customer entity = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(entity));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer entity = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException("CUSTOMER_NOT_FOUND", "Khách hàng không tồn tại"));

        if (!entity.getCustomerCode().equalsIgnoreCase(request.customerCode())
                && customerRepository.existsByCustomerCodeIgnoreCaseAndDeletedFalse(request.customerCode())) {
            throw new AppException("CUSTOMER_CODE_DUPLICATE", "Mã khách hàng đã tồn tại");
        }

        entity.setCustomerCode(request.customerCode());
        entity.setCustomerName(request.customerName());
        return customerMapper.toResponse(customerRepository.save(entity));
    }

    /**
     * Two-step soft delete (same pattern as ProjectTypeService).
     */
    @Transactional
    public DeleteResult softDelete(UUID id, boolean confirmed) {
        Customer entity = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException("CUSTOMER_NOT_FOUND", "Khách hàng không tồn tại"));

        long usageCount = customerRepository.countActiveProjectsByCustomerId(id);

        if (!confirmed && usageCount > 0) {
            return new DeleteResult(true, usageCount);
        }

        entity.setDeleted(true);
        customerRepository.save(entity);
        return new DeleteResult(false, usageCount);
    }

    public record DeleteResult(boolean inUse, long usageCount) {
    }
}
