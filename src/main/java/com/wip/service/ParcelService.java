package com.wip.service;

import com.wip.dto.ParcelDto;
import java.util.List;

public interface ParcelService {
    ParcelDto addParcel(ParcelDto parcelDto);
    List<ParcelDto> getAllParcels();
    ParcelDto getParcelById(Long id);
    List<ParcelDto> getParcelsByCustomerId(Long customerId);
    ParcelDto updateParcel(Long id, ParcelDto parcelDto);
    void deleteParcel(Long id);
    List<ParcelDto> search(String keyword);
}