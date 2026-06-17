package com.wip.service;

import com.wip.dto.ParcelDto;
import com.wip.entity.Customer;
import com.wip.entity.Parcel;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.CustomerRepository;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final CustomerRepository customerRepository;
    private final ShipmentRepository shipmentRepository;

    public ParcelServiceImpl(ParcelRepository parcelRepository,
                             CustomerRepository customerRepository,
                             ShipmentRepository shipmentRepository) {
        this.parcelRepository = parcelRepository;
        this.customerRepository = customerRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public ParcelDto addParcel(ParcelDto parcelDto) {
        Customer customer = customerRepository.findById(parcelDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Parcel parcel = new Parcel();
        parcel.setCustomer(customer);
        parcel.setReceiverPhone(customer.getPhone());
        parcel.setWeight(parcelDto.getWeight());
        parcel.setSourceAddress(parcelDto.getSourceAddress());
        parcel.setDestinationAddress(parcelDto.getDestinationAddress());
        parcel.setBookingDate(parcelDto.getBookingDate() != null ? parcelDto.getBookingDate() : LocalDate.now());

        return toDto(parcelRepository.save(parcel));
    }

    @Override
    public List<ParcelDto> getAllParcels() {
        return parcelRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ParcelDto getParcelById(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found"));
        return toDto(parcel);
    }

    @Override
    public List<ParcelDto> getParcelsByCustomerId(Long customerId) {
        return parcelRepository.findByCustomer_CustomerId(customerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ParcelDto updateParcel(Long id, ParcelDto parcelDto) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found"));

        Customer customer = customerRepository.findById(parcelDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        parcel.setCustomer(customer);
        parcel.setReceiverPhone(customer.getPhone());
        parcel.setWeight(parcelDto.getWeight());
        parcel.setSourceAddress(parcelDto.getSourceAddress());
        parcel.setDestinationAddress(parcelDto.getDestinationAddress());
        parcel.setBookingDate(parcelDto.getBookingDate());

        return toDto(parcelRepository.save(parcel));
    }

    @Override
    @Transactional
    public void deleteParcel(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found"));

        shipmentRepository.findByParcel_ParcelId(id).ifPresent(shipmentRepository::delete);

        parcelRepository.delete(parcel);
    }

    private ParcelDto toDto(Parcel parcel) {
        ParcelDto dto = new ParcelDto();
        dto.setParcelId(parcel.getParcelId());
        dto.setCustomerId(parcel.getCustomer() != null ? parcel.getCustomer().getCustomerId() : null);
        dto.setCustomerName(parcel.getCustomer() != null ? parcel.getCustomer().getCustomerName() : null);
        dto.setReceiverPhone(parcel.getReceiverPhone());
        dto.setWeight(parcel.getWeight());
        dto.setSourceAddress(parcel.getSourceAddress());
        dto.setDestinationAddress(parcel.getDestinationAddress());
        dto.setBookingDate(parcel.getBookingDate());

        if (parcel.getShipment() != null) {
            dto.setShipmentId(parcel.getShipment().getShipmentId());
            dto.setShipmentAvailable(true);
        } else {
            dto.setShipmentId(null);
            dto.setShipmentAvailable(false);
        }

        return dto;
    }
    @Override
    public List<ParcelDto> search(String keyword) {
        List<ParcelDto> parcels = getAllParcels();

        if (keyword == null || keyword.isBlank()) {
            return parcels;
        }

        String k = keyword.toLowerCase();

        return parcels.stream()
                .filter(p ->
                        (p.getParcelId() != null && String.valueOf(p.getParcelId()).contains(k)) ||
                        (p.getCustomerId() != null && String.valueOf(p.getCustomerId()).contains(k)) ||
                        (p.getCustomerName() != null && p.getCustomerName().toLowerCase().contains(k)) ||
                        (p.getReceiverPhone() != null && p.getReceiverPhone().toLowerCase().contains(k)) ||
                        (p.getSourceAddress() != null && p.getSourceAddress().toLowerCase().contains(k)) ||
                        (p.getDestinationAddress() != null && p.getDestinationAddress().toLowerCase().contains(k)))
                .toList();
    }
}