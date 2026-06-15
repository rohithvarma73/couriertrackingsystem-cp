package com.wip.service;

import com.wip.dto.ParcelDto;
import com.wip.entity.Parcel;
import com.wip.entity.Customer;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.CustomerRepository;
import com.wip.repository.ParcelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final CustomerRepository customerRepository;

    public ParcelServiceImpl(ParcelRepository parcelRepository, CustomerRepository customerRepository) {
        this.parcelRepository = parcelRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public ParcelDto addParcel(ParcelDto parcelDto) {
        Parcel parcel = toEntity(parcelDto);
        Customer customer = customerRepository.findById(parcelDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + parcelDto.getCustomerId()));
        parcel.setCustomer(customer);
        return toDto(parcelRepository.save(parcel));
    }

    @Override
    public List<ParcelDto> getAllParcels() {
        return parcelRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParcelDto getParcelById(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
        return toDto(parcel);
    }

    @Override
    public ParcelDto updateParcel(Long id, ParcelDto parcelDto) {
        Parcel existing = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));

        existing.setReceiverPhone(parcelDto.getReceiverPhone());
        existing.setWeight(parcelDto.getWeight());
        existing.setSourceAddress(parcelDto.getSourceAddress());
        existing.setDestinationAddress(parcelDto.getDestinationAddress());
        existing.setBookingDate(parcelDto.getBookingDate());

        if (parcelDto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(parcelDto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + parcelDto.getCustomerId()));
            existing.setCustomer(customer);
        }

        return toDto(parcelRepository.save(existing));
    }

    @Override
    public void deleteParcel(Long id) {
        Parcel existing = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
        parcelRepository.delete(existing);
    }

    private ParcelDto toDto(Parcel parcel) {
        ParcelDto dto = new ParcelDto();
        dto.setParcelId(parcel.getParcelId());
        dto.setReceiverPhone(parcel.getReceiverPhone());
        dto.setWeight(parcel.getWeight());
        dto.setSourceAddress(parcel.getSourceAddress());
        dto.setDestinationAddress(parcel.getDestinationAddress());
        dto.setBookingDate(parcel.getBookingDate());
        if (parcel.getCustomer() != null) {
            dto.setCustomerId(parcel.getCustomer().getCustomerId());
        }
        return dto;
    }

    private Parcel toEntity(ParcelDto dto) {
        Parcel parcel = new Parcel();
        parcel.setParcelId(dto.getParcelId());
        parcel.setReceiverPhone(dto.getReceiverPhone());
        parcel.setWeight(dto.getWeight());
        parcel.setSourceAddress(dto.getSourceAddress());
        parcel.setDestinationAddress(dto.getDestinationAddress());
        parcel.setBookingDate(dto.getBookingDate());
        return parcel;
    }
}