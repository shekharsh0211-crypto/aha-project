package com.ahatravel.customer.dto.response;

import com.ahatravel.customer.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {

    private Long id;
    private String referenceNumber;
    private BookingStatus status;
    private String pickupLocation;
    private String dropoffLocation;
    private LocalDateTime pickupDatetime;
    private LocalDateTime dropoffDatetime;
    private Integer passengerCount;
    private String specialInstructions;
    private BigDecimal totalAmount;
    private String currency;
    private String cancellationReason;
    private Long userId;
    private String userFullName;
    private Long vehicleId;
    private String vehiclePlate;
    private String vehicleDescription;
    private Long rebookedFromId;
    private List<StatusHistoryResponse> statusHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class StatusHistoryResponse {
        private Long id;
        private BookingStatus fromStatus;
        private BookingStatus toStatus;
        private String changedBy;
        private String remarks;
        private LocalDateTime createdAt;
    }
}
