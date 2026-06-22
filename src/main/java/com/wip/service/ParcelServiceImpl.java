package com.wip.service;

import com.wip.dto.ParcelDto;
import com.wip.entity.AppUser;
import com.wip.entity.Customer;
import com.wip.entity.Parcel;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.AppUserRepository;
import com.wip.repository.CustomerRepository;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import com.wip.security.CurrentUserUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for managing parcel business logic.
 *
 * <p>Handles the creation, retrieval, updating, and deletion of parcels. It enforces
 * data isolation using the authenticated user context ({@link CurrentUserUtil}), ensuring
 * that regular users can only interact with their own parcels while administrators
 * have global access.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final CustomerRepository customerRepository;
    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository appUserRepository;

    /**
     * Constructs a {@code ParcelServiceImpl} with the required repository dependencies.
     *
     * @param parcelRepository   the repository for parcel data access
     * @param customerRepository the repository for customer data access
     * @param shipmentRepository the repository for shipment data access
     * @param appUserRepository  the repository for user data access
     */
    public ParcelServiceImpl(ParcelRepository parcelRepository,
                             CustomerRepository customerRepository,
                             ShipmentRepository shipmentRepository,
                             AppUserRepository appUserRepository) {
        this.parcelRepository = parcelRepository;
        this.customerRepository = customerRepository;
        this.shipmentRepository = shipmentRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Creates a new parcel booking in the system.
     *
     * <p>If the current user is not an administrator, the parcel is automatically
     * linked to their profile. Throws an exception if the associated customer cannot be found.</p>
     *
     * @param parcelDto the data transfer object containing parcel details
     * @return the newly saved parcel data as a {@link ParcelDto}
     * @throws ResourceNotFoundException if the user or customer is not found
     */
    @Override
    public ParcelDto addParcel(ParcelDto parcelDto) {
        String username = CurrentUserUtil.getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long targetCustomerId = parcelDto.getCustomerId();
        if (!CurrentUserUtil.isAdmin() && currentUser.getCustomer() != null) {
            targetCustomerId = currentUser.getCustomer().getCustomerId();
        }

        if (targetCustomerId == null) {
            throw new ResourceNotFoundException("Customer not specified or user has no profile");
        }

        Customer customer = customerRepository.findById(targetCustomerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!CurrentUserUtil.isAdmin() && (customer.getCreatedBy() == null || !username.equals(customer.getCreatedBy().getUsername()))) {
            throw new ResourceNotFoundException("Customer not found");
        }

        Parcel parcel = new Parcel();
        parcel.setCustomer(customer);
        parcel.setCreatedBy(currentUser);
        parcel.setReceiverPhone(customer.getPhone());
        parcel.setWeight(parcelDto.getWeight());
        parcel.setSourceAddress(parcelDto.getSourceAddress());
        parcel.setDestinationAddress(parcelDto.getDestinationAddress());
        parcel.setBookingDate(parcelDto.getBookingDate() != null ? parcelDto.getBookingDate() : LocalDate.now());

        return toDto(parcelRepository.save(parcel));
    }

    /**
     * Retrieves all parcels accessible to the current user.
     *
     * <p>Administrators receive all parcels in the system, whereas regular users
     * only receive the parcels they created.</p>
     *
     * @return a list of accessible parcels
     */
    @Override
    public List<ParcelDto> getAllParcels() {
        if (CurrentUserUtil.isAdmin()) {
            return parcelRepository.findAll().stream().map(this::toDto).toList();
        }
        String username = CurrentUserUtil.getCurrentUsername();
        return parcelRepository.findByCreatedBy_Username(username)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Retrieves a single parcel by its ID.
     *
     * <p>Enforces access control to ensure users can only view their own parcels.</p>
     *
     * @param id the unique identifier of the parcel
     * @return the requested parcel data as a {@link ParcelDto}
     * @throws ResourceNotFoundException if the parcel does not exist or access is denied
     */
    @Override
    public ParcelDto getParcelById(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (parcel.getCreatedBy() == null || !username.equals(parcel.getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Parcel not found");
            }
        }

        return toDto(parcel);
    }

    /**
     * Retrieves all parcels belonging to a specific customer.
     *
     * @param customerId the unique identifier of the customer
     * @return a list of parcels associated with the specified customer
     * @throws ResourceNotFoundException if the customer does not exist or access is denied
     */
    @Override
    public List<ParcelDto> getParcelsByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (customer.getCreatedBy() == null || !username.equals(customer.getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Customer not found");
            }
        }

        return parcelRepository.findByCustomer_CustomerId(customerId)
                .stream()
                .filter(p -> CurrentUserUtil.isAdmin() || 
                             (p.getCreatedBy() != null && CurrentUserUtil.getCurrentUsername().equals(p.getCreatedBy().getUsername())))
                .map(this::toDto)
                .toList();
    }

    /**
     * Updates an existing parcel's information.
     *
     * @param id        the unique identifier of the parcel to update
     * @param parcelDto the new parcel data
     * @return the updated parcel data as a {@link ParcelDto}
     * @throws ResourceNotFoundException if the parcel or customer does not exist, or access is denied
     */
    @Override
    public ParcelDto updateParcel(Long id, ParcelDto parcelDto) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (parcel.getCreatedBy() == null || !username.equals(parcel.getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Parcel not found");
            }
        }

        Customer customer = customerRepository.findById(parcelDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (customer.getCreatedBy() == null || !username.equals(customer.getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Customer not found");
            }
        }

        parcel.setCustomer(customer);
        parcel.setReceiverPhone(customer.getPhone());
        parcel.setWeight(parcelDto.getWeight());
        parcel.setSourceAddress(parcelDto.getSourceAddress());
        parcel.setDestinationAddress(parcelDto.getDestinationAddress());
        parcel.setBookingDate(parcelDto.getBookingDate());

        return toDto(parcelRepository.save(parcel));
    }

    /**
     * Deletes a parcel and its associated shipments from the system.
     *
     * <p>This operation is transactional to ensure data consistency when removing
     * dependent shipment records.</p>
     *
     * @param id the unique identifier of the parcel to delete
     * @throws ResourceNotFoundException if the parcel does not exist or access is denied
     */
    @Override
    @Transactional
    public void deleteParcel(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (parcel.getCreatedBy() == null || !username.equals(parcel.getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Parcel not found");
            }
        }

        shipmentRepository.findByParcel_ParcelId(id).ifPresent(shipmentRepository::delete);
        parcelRepository.delete(parcel);
    }

    /**
     * Searches for parcels using a keyword.
     *
     * <p>Filters the accessible parcels based on ID, customer name, phone number,
     * or addresses.</p>
     *
     * @param keyword the search string
     * @return a list of parcels matching the search criteria
     */
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

    /**
     * Converts a {@link Parcel} entity into a {@link ParcelDto}.
     *
     * @param parcel the parcel entity to convert
     * @return the mapped data transfer object
     */
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
}
