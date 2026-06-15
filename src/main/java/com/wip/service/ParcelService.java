package com.wip.service;

import com.wip.dto.ParcelDto;

import java.util.List;

public interface ParcelService {
    ParcelDto addParcel(ParcelDto parcelDto);
    List<ParcelDto> getAllParcels();
    ParcelDto getParcelById(Long id);
    ParcelDto updateParcel(Long id, ParcelDto parcelDto);
    void deleteParcel(Long id);
}