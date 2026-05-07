package com.internal.projectmgmt.mapper;

import com.internal.projectmgmt.dto.customer.CustomerRequest;
import com.internal.projectmgmt.dto.customer.CustomerResponse;
import com.internal.projectmgmt.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .customerCode(request.customerCode())
                .customerName(request.customerName())
                .build();
    }

    public CustomerResponse toResponse(Customer entity) {
        return new CustomerResponse(entity.getId(), entity.getCustomerCode(), entity.getCustomerName());
    }
}
