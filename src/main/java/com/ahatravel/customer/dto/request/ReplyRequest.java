package com.ahatravel.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReplyRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 5000)
    private String message;
}
