package com.ahatravel.customer.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefDataResponse {

    private Long id;
    private String category;
    private String code;
    private String label;
    private String description;
    private Integer sortOrder;
}
