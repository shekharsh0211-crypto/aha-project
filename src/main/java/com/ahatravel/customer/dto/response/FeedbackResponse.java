package com.ahatravel.customer.dto.response;

import com.ahatravel.customer.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FeedbackResponse {

    private Long id;
    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private String userFullName;
    private String subject;
    private String message;
    private Integer rating;
    private TicketStatus ticketStatus;
    private Boolean isFeedback;
    private Long parentId;
    private List<FeedbackResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
