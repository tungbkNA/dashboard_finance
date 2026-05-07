package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.customer.CustomerRequest;
import com.internal.projectmgmt.entity.Customer;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.CustomerMapper;
import com.internal.projectmgmt.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    // T030 — duplicate customerCode throws AppException
    @Test
    void create_shouldThrow_whenCustomerCodeDuplicate() {
        CustomerRequest request = new CustomerRequest("CUST-001", "Công ty ABC");
        when(customerRepository.existsByCustomerCodeIgnoreCaseAndDeletedFalse("CUST-001"))
                .thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Mã khách hàng đã tồn tại")
                .extracting("code").isEqualTo("CUSTOMER_CODE_DUPLICATE");
    }

    // T031 — softDelete in-use: returns inUse=true, does NOT soft delete
    @Test
    void softDelete_shouldReturnInUseWarning_whenInUse() {
        UUID id = UUID.randomUUID();
        Customer entity = Customer.builder().id(id).customerCode("C1").customerName("KH1").build();
        when(customerRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(customerRepository.countActiveProjectsByCustomerId(id)).thenReturn(2L);

        CustomerService.DeleteResult result = customerService.softDelete(id, false);

        assertThat(result.inUse()).isTrue();
        assertThat(result.usageCount()).isEqualTo(2L);
        verify(customerRepository, never()).save(any());
    }

    // T031 — softDelete with confirmed=true performs soft delete regardless of
    // usage
    @Test
    void softDelete_shouldSoftDelete_whenConfirmed() {
        UUID id = UUID.randomUUID();
        Customer entity = Customer.builder().id(id).customerCode("C1").customerName("KH1").build();
        when(customerRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(customerRepository.countActiveProjectsByCustomerId(id)).thenReturn(4L);
        when(customerRepository.save(any())).thenReturn(entity);

        CustomerService.DeleteResult result = customerService.softDelete(id, true);

        assertThat(result.inUse()).isFalse();
        assertThat(entity.isDeleted()).isTrue();
        verify(customerRepository).save(entity);
    }
}
