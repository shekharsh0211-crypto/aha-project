package com.ahatravel.customer.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateBookingRequest {

    @NotBlank(message = "Pickup location is required")
    @Size(max = 500)
    private String pickupLocation;

    @NotBlank(message = "Dropoff location is required")
    @Size(max = 500)
    private String dropoffLocation;

    @NotNull(message = "Pickup datetime is required")
    @Future(message = "Pickup datetime must be in the future")
    private LocalDateTime pickupDatetime;

    @Min(value = 1, message = "Passenger count must be at least 1")
    @Max(value = 50)
    private Integer passengerCount;

    @Size(max = 200)
    private String passengerName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String passengerPhone;

    @Size(max = 1000)
    private String specialInstructions;

    private Long vehicleId;

    @Size(max = 100)
    private String vehicleType;

    @Size(max = 100)
    private String tripType;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalAmount;

    @Size(max = 10)
    private String currency;
}
