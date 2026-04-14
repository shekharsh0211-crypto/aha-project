package com.ahatravel.customer.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateFeedbackRequest {

    private Long bookingId;

    @NotBlank(message = "Subject is required")
    @Size(max = 300)
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 5000)
    private String message;

    @Min(1) @Max(5)
    private Integer rating;

    private Boolean isFeedback = false;
}
