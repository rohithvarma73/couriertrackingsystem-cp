package com.wip.service;

import com.wip.dto.ShipmentDto;
import com.wip.entity.AppUser;
import com.wip.entity.Parcel;
import com.wip.entity.Shipment;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.AppUserRepository;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import com.wip.repository.TrackingUpdateRepository;
import com.wip.security.CurrentUserUtil;
import com.wip.util.TrackingNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing shipment business logic.
 *
 * <p>Handles the creation, tracking, updating, and deletion of shipments.
 * Enforces role-based access control, allowing only administrators to modify
 * shipments while restricting regular users to read-only access for their own data.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ParcelRepository parcelRepository;
    private final TrackingUpdateRepository trackingUpdateRepository;
    private final AppUserRepository appUserRepository;

    /**
     * Constructs a {@code ShipmentServiceImpl} with the required dependencies.
     *
     * @param shipmentRepository       the repository for shipment data
     * @param parcelRepository         the repository for parcel data
     * @param trackingUpdateRepository the repository for tracking update data
     * @param appUserRepository        the repository for user data
     */
    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               ParcelRepository parcelRepository,
                               TrackingUpdateRepository trackingUpdateRepository,
                               AppUserRepository appUserRepository) {
        this.shipmentRepository = shipmentRepository;
        this.parcelRepository = parcelRepository;
        this.trackingUpdateRepository = trackingUpdateRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Creates a new shipment for an existing parcel and generates a tracking number.
     *
     * @param parcelId the unique identifier of the parcel to ship
     * @return the created shipment details
     * @throws IllegalStateException if the user is not an administrator, or if a shipment already exists
     * @throws ResourceNotFoundException if the parcel or user is not found
     */
    @Override
    public ShipmentDto addShipment(Long parcelId) {
        if (!CurrentUserUtil.isAdmin()) {
            throw new IllegalStateException("Only administrators can add shipments");
        }

        String username = CurrentUserUtil.getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found"));

        Optional<Shipment> existingShipment = shipmentRepository.findByParcel_ParcelId(parcelId);
        if (existingShipment.isPresent()) {
            throw new IllegalStateException("Shipment already exists for this parcel. View the existing shipment instead.");
        }

        Shipment shipment = new Shipment();
        shipment.setParcel(parcel);
        shipment.setCreatedBy(currentUser);
        shipment.setTrackingNumber(TrackingNumberGenerator.generateTrackingNumber());
        shipment.setShipmentDate(LocalDate.now());
        shipment.setCurrentLocation(parcel.getSourceAddress());
        shipment.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));

        return toDto(shipmentRepository.save(shipment));
    }

    /**
     * Retrieves all shipments accessible to the current user.
     *
     * @return a list of all accessible shipments
     */
    @Override
    public List<ShipmentDto> getAllShipments() {
        if (CurrentUserUtil.isAdmin()) {
            return shipmentRepository.findAll().stream().map(this::toDto).toList();
        }
        String username = CurrentUserUtil.getCurrentUsername();
        return shipmentRepository.findByParcel_Customer_CreatedBy_Username(username)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Retrieves a shipment by its ID.
     *
     * @param id the unique identifier of the shipment
     * @return the shipment details
     * @throws ResourceNotFoundException if the shipment does not exist or access is denied
     */
    @Override
    public ShipmentDto getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (shipment.getParcel() == null || shipment.getParcel().getCustomer() == null || 
                shipment.getParcel().getCustomer().getCreatedBy() == null || 
                !username.equals(shipment.getParcel().getCustomer().getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Shipment not found");
            }
        }

        return toDto(shipment);
    }

    /**
     * Retrieves a shipment by its tracking number.
     *
     * @param trackingNumber the unique alphanumeric tracking number
     * @return the shipment details
     * @throws ResourceNotFoundException if the shipment does not exist or access is denied
     */
    @Override
    public ShipmentDto getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (shipment.getParcel() == null || shipment.getParcel().getCustomer() == null || 
                shipment.getParcel().getCustomer().getCreatedBy() == null || 
                !username.equals(shipment.getParcel().getCustomer().getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Shipment not found");
            }
        }

        return toDto(shipment);
    }

    /**
     * Updates the current location of a shipment.
     *
     * @param id              the unique identifier of the shipment
     * @param currentLocation the new location of the shipment
     * @return the updated shipment details
     * @throws IllegalStateException if the user is not an administrator
     * @throws ResourceNotFoundException if the shipment does not exist
     */
    @Override
    public ShipmentDto updateShipmentLocation(Long id, String currentLocation) {
        if (!CurrentUserUtil.isAdmin()) {
            throw new IllegalStateException("Only administrators can update shipments");
        }
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        shipment.setCurrentLocation(currentLocation);
        return toDto(shipmentRepository.save(shipment));
    }

    /**
     * Updates the shipment dates and current location.
     *
     * @param id          the unique identifier of the shipment
     * @param shipmentDto the updated shipment details
     * @return the updated shipment details
     * @throws IllegalStateException if the user is not an administrator
     * @throws ResourceNotFoundException if the shipment does not exist
     */
    @Override
    public ShipmentDto updateShipment(Long id, ShipmentDto shipmentDto) {
        if (!CurrentUserUtil.isAdmin()) {
            throw new IllegalStateException("Only administrators can update shipments");
        }
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        shipment.setCurrentLocation(shipmentDto.getCurrentLocation());
        shipment.setEstimatedDeliveryDate(shipmentDto.getEstimatedDeliveryDate());

        if (shipmentDto.getShipmentDate() != null) {
            shipment.setShipmentDate(shipmentDto.getShipmentDate());
        }

        return toDto(shipmentRepository.save(shipment));
    }

    /**
     * Deletes a shipment and its associated tracking updates.
     *
     * @param id the unique identifier of the shipment to delete
     * @throws IllegalStateException if the user is not an administrator
     * @throws ResourceNotFoundException if the shipment does not exist
     */
    @Transactional
    @Override
    public void deleteShipment(Long id) {
        if (!CurrentUserUtil.isAdmin()) {
            throw new IllegalStateException("Only administrators can delete shipments");
        }
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        trackingUpdateRepository.deleteByShipment_ShipmentId(id);
        shipmentRepository.delete(shipment);
    }

    /**
     * Retrieves the shipment associated with a specific parcel.
     *
     * @param parcelId the unique identifier of the parcel
     * @return the associated shipment, or null if not found or unauthorized
     */
    @Override
    public ShipmentDto getShipmentByParcelId(Long parcelId) {
        return shipmentRepository.findByParcel_ParcelId(parcelId)
                .filter(s -> CurrentUserUtil.isAdmin() || 
                             (s.getParcel() != null && s.getParcel().getCustomer() != null && 
                              s.getParcel().getCustomer().getCreatedBy() != null && 
                              CurrentUserUtil.getCurrentUsername().equals(s.getParcel().getCustomer().getCreatedBy().getUsername())))
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Searches for shipments using a keyword.
     *
     * @param keyword the search string
     * @return a list of shipments matching the criteria
     */
    @Override
    public List<ShipmentDto> search(String keyword) {
        List<ShipmentDto> shipments = getAllShipments();
        if (keyword == null || keyword.isBlank()) {
            return shipments;
        }

        String k = keyword.toLowerCase();
        return shipments.stream()
                .filter(s ->
                        (s.getShipmentId() != null && String.valueOf(s.getShipmentId()).contains(k)) ||
                        (s.getParcelId() != null && String.valueOf(s.getParcelId()).contains(k)) ||
                        (s.getTrackingNumber() != null && s.getTrackingNumber().toLowerCase().contains(k)) ||
                        (s.getCustomerName() != null && s.getCustomerName().toLowerCase().contains(k)) ||
                        (s.getReceiverPhone() != null && s.getReceiverPhone().toLowerCase().contains(k)) ||
                        (s.getCurrentLocation() != null && s.getCurrentLocation().toLowerCase().contains(k)))
                .toList();
    }

    /**
     * Converts a {@link Shipment} entity to a {@link ShipmentDto}.
     *
     * @param shipment the shipment entity
     * @return the converted DTO
     */
    private ShipmentDto toDto(Shipment shipment) {
        ShipmentDto dto = new ShipmentDto();
        dto.setShipmentId(shipment.getShipmentId());
        dto.setParcelId(shipment.getParcel() != null ? shipment.getParcel().getParcelId() : null);
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setShipmentDate(shipment.getShipmentDate());
        dto.setCurrentLocation(shipment.getCurrentLocation());
        dto.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        dto.setCustomerName(
                shipment.getParcel() != null && shipment.getParcel().getCustomer() != null
                        ? shipment.getParcel().getCustomer().getCustomerName()
                        : null
        );
        dto.setReceiverPhone(shipment.getParcel() != null ? shipment.getParcel().getReceiverPhone() : null);
        return dto;
    }
}
