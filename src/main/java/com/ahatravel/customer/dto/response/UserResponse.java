package com.ahatravel.customer.dto.response;

import com.ahatravel.customer.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private UserRole role;
    private Boolean active;
    private Boolean emailVerified;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
}
