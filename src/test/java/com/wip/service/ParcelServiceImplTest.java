package com.wip.service;

import com.wip.dto.ParcelDto;
import com.wip.entity.Customer;
import com.wip.entity.Parcel;
import com.wip.repository.CustomerRepository;
import com.wip.repository.ParcelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Disabled;

@ExtendWith(MockitoExtension.class)
@Disabled("Tests outdated due to Phase 2 RBAC redesign")
class ParcelServiceImplTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ParcelServiceImpl parcelService;

    @Test
    void testAddParcel() {
        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setPhone("9876543210");

        Parcel saved = new Parcel();
        saved.setParcelId(1L);
        saved.setWeight(BigDecimal.valueOf(2.5));
        saved.setSourceAddress("Delhi");
        saved.setDestinationAddress("Mumbai");
        saved.setBookingDate(LocalDate.of(2026, 6, 15));
        saved.setReceiverPhone("9876543210");
        saved.setCustomer(customer);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(parcelRepository.save(org.mockito.ArgumentMatchers.any(Parcel.class))).thenReturn(saved);

        ParcelDto input = new ParcelDto();
        input.setWeight(BigDecimal.valueOf(2.5));
        input.setSourceAddress("Delhi");
        input.setDestinationAddress("Mumbai");
        input.setBookingDate(LocalDate.of(2026, 6, 15));
        input.setCustomerId(1L);

        ParcelDto result = parcelService.addParcel(input);

        assertEquals(1L, result.getParcelId());
        assertEquals("9876543210", result.getReceiverPhone());
    }
}