package com.ahatravel.customer.config;

import com.ahatravel.customer.entity.*;
import com.ahatravel.customer.enums.BookingStatus;
import com.ahatravel.customer.enums.UserRole;
import com.ahatravel.customer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RefDataRepository refDataRepository;
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedCompany();
        seedAdminUser();
        seedRefData();
        seedVehicles();
        seedSampleBookings();
    }

    private void seedCompany() {
        if (!companyRepository.existsByCode("AHA001")) {
            Company company = Company.builder()
                    .name("AHA Travel")
                    .code("AHA001")
                    .email("info@ahatravel.com")
                    .phone("+1-555-0100")
                    .address("123 Travel Lane, San Francisco, CA 94105")
                    .build();
            companyRepository.save(company);
            log.info("Default company seeded.");
        }
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@ahatravel.com")) {
            Company company = companyRepository.findByCode("AHA001").orElse(null);
            User admin = User.builder()
                    .email("admin@ahatravel.com")
                    .passwordHash(passwordEncoder.encode("Admin@1234"))
                    .firstName("System")
                    .lastName("Admin")
                    .role(UserRole.ROLE_ADMIN)
                    .company(company)
                    .active(true)
                    .emailVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin user seeded: admin@ahatravel.com / Admin@1234");
        }
    }

    private void seedRefData() {
        if (refDataRepository.count() == 0) {
            List<RefData> entries = List.of(
                    buildRef("VEHICLE_TYPE", "SEDAN",      "Sedan",             1),
                    buildRef("VEHICLE_TYPE", "SUV",        "SUV",               2),
                    buildRef("VEHICLE_TYPE", "VAN",        "Van",               3),
                    buildRef("VEHICLE_TYPE", "LUXURY",     "Luxury",            4),
                    buildRef("VEHICLE_TYPE", "BUS",        "Bus",               5),
                    buildRef("TRIP_TYPE",    "AIRPORT",    "Airport Transfer",  1),
                    buildRef("TRIP_TYPE",    "TRAIN",      "Train Transfer",    2),
                    buildRef("TRIP_TYPE",    "HOTEL",      "Hotel Transfer",    3),
                    buildRef("TRIP_TYPE",    "CITY_TOUR",  "City Tour",         4),
                    buildRef("TRIP_TYPE",    "OLD_STATION","Old Station",       5),
                    buildRef("CURRENCY",     "USD",        "US Dollar",         1),
                    buildRef("CURRENCY",     "EUR",        "Euro",              2),
                    buildRef("CURRENCY",     "GBP",        "British Pound",     3),
                    buildRef("BOOKING_CANCELLATION_REASON","CHANGE_OF_PLANS","Change of plans",    1),
                    buildRef("BOOKING_CANCELLATION_REASON","WRONG_DATE",     "Wrong date selected", 2),
                    buildRef("BOOKING_CANCELLATION_REASON","OTHER",          "Other",               3)
            );
            refDataRepository.saveAll(entries);
            log.info("Reference data seeded.");
        }
    }

    private void seedVehicles() {
        if (vehicleRepository.count() > 0) return;
        List<Vehicle> vehicles = List.of(
                Vehicle.builder().plateNumber("MH01AB1234").make("Maruti").model("Swift Dzire").year(2022).vehicleType("Sedan").capacity(4).active(true).build(),
                Vehicle.builder().plateNumber("MH02CD5678").make("Toyota").model("Innova Crysta").year(2023).vehicleType("Innova Crysta").capacity(7).active(true).build(),
                Vehicle.builder().plateNumber("MH03EF9012").make("Maruti").model("Ertiga").year(2022).vehicleType("MUV/Ertiga").capacity(7).active(true).build(),
                Vehicle.builder().plateNumber("MH04GH3456").make("Mercedes").model("E-Class").year(2023).vehicleType("Luxury").capacity(4).active(true).build()
        );
        vehicleRepository.saveAll(vehicles);
        log.info("Sample vehicles seeded.");
    }

    private void seedSampleBookings() {
        if (bookingRepository.findByReferenceNumber("BK-20240342").isPresent()) return;

        User admin = userRepository.findByEmail("admin@ahatravel.com").orElse(null);
        if (admin == null) return;

        List<Vehicle> vehicles = vehicleRepository.findAll();
        Map<String, Vehicle> byType = new java.util.HashMap<>();
        vehicles.forEach(v -> byType.put(v.getVehicleType(), v));

        record BookingSeed(String ref, String pickup, String dropoff, String datetime, String vtype,
                           BookingStatus status, BigDecimal amount, String cancelReason) {}

        List<BookingSeed> seeds = List.of(
            new BookingSeed("BK-20240342", "Downtown Office",     "International Airport",  "2024-04-25T10:10:00", "Sedan",         BookingStatus.COMPLETED, new BigDecimal("347.00"),  null),
            new BookingSeed("BK-20240369", "Pawa Road Station",   "Airport Terminal 1",     "2024-04-22T09:00:00", "Innova Crysta", BookingStatus.COMPLETED, new BigDecimal("820.00"),  null),
            new BookingSeed("BK-20240385", "CST Station",         "Bandra West",            "2024-04-18T12:00:00", "Sedan",         BookingStatus.COMPLETED, new BigDecimal("360.00"),  null),
            new BookingSeed("BK-20240371", "Goregaon East",       "BKC",                    "2024-04-18T08:30:00", "MUV/Ertiga",    BookingStatus.CANCELLED, null, "Change of plans"),
            new BookingSeed("BK-20240412", "Hotel Taj Colaba",    "Chhatrapati Airport",    "2024-04-28T06:00:00", "Luxury",        BookingStatus.CONFIRMED,  new BigDecimal("1200.00"), null),
            new BookingSeed("BK-20240420", "Andheri Station",     "Powai",                  "2024-04-30T14:30:00", "Innova Crysta", BookingStatus.PENDING,    new BigDecimal("450.00"),  null)
        );

        for (BookingSeed s : seeds) {
            Booking booking = Booking.builder()
                    .referenceNumber(s.ref())
                    .user(admin)
                    .vehicle(byType.get(s.vtype()))
                    .pickupLocation(s.pickup())
                    .dropoffLocation(s.dropoff())
                    .pickupDatetime(LocalDateTime.parse(s.datetime()))
                    .passengerCount(2)
                    .status(s.status())
                    .totalAmount(s.amount())
                    .currency("USD")
                    .cancellationReason(s.cancelReason())
                    .build();

            BookingStatusHistory history = BookingStatusHistory.builder()
                    .booking(booking)
                    .toStatus(s.status())
                    .changedBy("system")
                    .remarks("Initial status")
                    .build();

            booking.getStatusHistory().add(history);
            bookingRepository.save(booking);
        }
        log.info("Sample bookings seeded.");
    }

    private RefData buildRef(String category, String code, String label, int order) {
        return RefData.builder()
                .category(category)
                .code(code)
                .label(label)
                .sortOrder(order)
                .active(true)
                .build();
    }
}
