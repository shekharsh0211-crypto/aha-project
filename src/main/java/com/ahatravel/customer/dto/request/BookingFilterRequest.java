package com.ahatravel.customer.dto.request;

import com.ahatravel.customer.enums.BookingStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class BookingFilterRequest {

    private BookingStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    private Long userId;

    private int page = 0;

    private int size = 10;

    private String sortBy = "createdAt";

    private String sortDir = "desc";
}
