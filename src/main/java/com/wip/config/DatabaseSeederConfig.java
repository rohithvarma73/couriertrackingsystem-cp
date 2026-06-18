package com.wip.config;

import com.wip.entity.*;
import com.wip.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DatabaseSeederConfig {

    @Bean
    public CommandLineRunner initDatabase(AppUserRepository appUserRepository,
                                          CustomerRepository customerRepository,
                                          ParcelRepository parcelRepository,
                                          ShipmentRepository shipmentRepository,
                                          TrackingUpdateRepository trackingUpdateRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            if (appUserRepository.count() == 0) {
                System.out.println("Initializing Nexus Logistics Database with real-world mock data...");

                // 1. Create Admin
                AppUser adminUser = new AppUser();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setEmail("hq@nexuslogistics.com");
                adminUser.setRole("ADMIN");
                appUserRepository.save(adminUser);

                // 2. Create Normal User 1 (John)
                AppUser user1 = new AppUser();
                user1.setUsername("user");
                user1.setPassword(passwordEncoder.encode("user123"));
                user1.setEmail("john.doe@example.com");
                user1.setRole("USER");

                Customer customer1 = new Customer();
                customer1.setCustomerName("John Doe");
                customer1.setEmail("john.doe@example.com");
                customer1.setPhone("555-0100");
                customer1.setAddress("123 Tech Boulevard, Silicon Valley");
                
                user1.setCustomer(customer1);
                customer1.setCreatedBy(user1);
                appUserRepository.save(user1); // cascades customer1

                // 3. Create Normal User 2 (Jane)
                AppUser user2 = new AppUser();
                user2.setUsername("jane");
                user2.setPassword(passwordEncoder.encode("jane123"));
                user2.setEmail("jane.smith@example.com");
                user2.setRole("USER");

                Customer customer2 = new Customer();
                customer2.setCustomerName("Jane Smith");
                customer2.setEmail("jane.smith@example.com");
                customer2.setPhone("555-0200");
                customer2.setAddress("456 Innovation Drive, Seattle");
                
                user2.setCustomer(customer2);
                customer2.setCreatedBy(user2);
                appUserRepository.save(user2);

                // 4. Create Parcels for John
                Parcel parcel1 = new Parcel();
                parcel1.setCustomer(customer1);
                parcel1.setCreatedBy(user1);
                parcel1.setReceiverPhone("555-0999");
                parcel1.setWeight(new BigDecimal("2.5"));
                parcel1.setSourceAddress("123 Tech Boulevard, Silicon Valley");
                parcel1.setDestinationAddress("789 Cloud Ave, Austin");
                parcel1.setBookingDate(LocalDate.now().minusDays(2));
                parcelRepository.save(parcel1);

                Parcel parcel2 = new Parcel();
                parcel2.setCustomer(customer1);
                parcel2.setCreatedBy(user1);
                parcel2.setReceiverPhone("555-0888");
                parcel2.setWeight(new BigDecimal("0.5"));
                parcel2.setSourceAddress("123 Tech Boulevard, Silicon Valley");
                parcel2.setDestinationAddress("321 Data Lane, Boston");
                parcel2.setBookingDate(LocalDate.now());
                parcelRepository.save(parcel2);

                // 5. Create Parcels for Jane
                Parcel parcel3 = new Parcel();
                parcel3.setCustomer(customer2);
                parcel3.setCreatedBy(user2);
                parcel3.setReceiverPhone("555-0777");
                parcel3.setWeight(new BigDecimal("15.0"));
                parcel3.setSourceAddress("456 Innovation Drive, Seattle");
                parcel3.setDestinationAddress("123 Tech Boulevard, Silicon Valley");
                parcel3.setBookingDate(LocalDate.now().minusDays(1));
                parcelRepository.save(parcel3);

                // 6. Create Shipments (Admin action simulated)
                Shipment shipment1 = new Shipment();
                shipment1.setParcel(parcel1);
                shipment1.setCreatedBy(adminUser);
                shipment1.setTrackingNumber("TRK-" + System.currentTimeMillis());
                shipment1.setShipmentDate(LocalDate.now().minusDays(1));
                shipment1.setEstimatedDeliveryDate(LocalDate.now().plusDays(2));
                shipment1.setCurrentLocation("Distribution Hub Alpha");
                shipmentRepository.save(shipment1);

                Shipment shipment2 = new Shipment();
                shipment2.setParcel(parcel3);
                shipment2.setCreatedBy(adminUser);
                shipment2.setTrackingNumber("TRK-" + (System.currentTimeMillis() + 1000));
                shipment2.setShipmentDate(LocalDate.now());
                shipment2.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
                shipment2.setCurrentLocation("Origin Terminal Seattle");
                shipmentRepository.save(shipment2);

                // 7. Create Tracking Updates
                TrackingUpdate update1 = new TrackingUpdate();
                update1.setShipment(shipment1);
                update1.setCreatedBy(adminUser);
                update1.setDeliveryStatus("Booked");
                update1.setLocation("Silicon Valley Origin");
                update1.setRemarks("Parcel scanned into system");
                update1.setCreatedAt(LocalDateTime.now().minusHours(24));
                trackingUpdateRepository.save(update1);

                TrackingUpdate update2 = new TrackingUpdate();
                update2.setShipment(shipment1);
                update2.setCreatedBy(adminUser);
                update2.setDeliveryStatus("In Transit");
                update2.setLocation("Distribution Hub Alpha");
                update2.setRemarks("Departed origin facility");
                update2.setCreatedAt(LocalDateTime.now().minusHours(12));
                trackingUpdateRepository.save(update2);
                
                System.out.println("Nexus Logistics Database seeded successfully!");
            }
        };
    }
}
