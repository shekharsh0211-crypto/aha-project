package com.ahatravel.customer.entity;

import com.ahatravel.customer.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_user_id", columnList = "user_id"),
        @Index(name = "idx_bookings_status", columnList = "status"),
        @Index(name = "idx_bookings_pickup_date", columnList = "pickup_datetime"),
        @Index(name = "idx_bookings_reference", columnList = "reference_number"),
        @Index(name = "idx_bookings_vehicle_id", columnList = "vehicle_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 30)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "pickup_location", nullable = false, length = 500)
    private String pickupLocation;

    @Column(name = "dropoff_location", nullable = false, length = 500)
    private String dropoffLocation;

    @Column(name = "pickup_datetime", nullable = false)
    private LocalDateTime pickupDatetime;

    @Column(name = "dropoff_datetime")
    private LocalDateTime dropoffDatetime;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "passenger_name", length = 200)
    private String passengerName;

    @Column(name = "passenger_phone", length = 20)
    private String passengerPhone;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rebooked_from_id")
    private Booking rebookedFrom;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FeedbackSupport> feedbacks = new ArrayList<>();
}
