package com.ahatravel.customer.controller;

import com.ahatravel.customer.dto.response.ApiResponse;
import com.ahatravel.customer.dto.response.RefDataResponse;
import com.ahatravel.customer.service.RefDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ref-data")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reference Data", description = "Cached reference data lookup")
public class RefDataController {

    private final RefDataService refDataService;

    @GetMapping
    @Operation(summary = "Get all active reference data")
    public ResponseEntity<ApiResponse<List<RefDataResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(refDataService.getAll()));
    }

    @GetMapping("/{category}")
    @Operation(summary = "Get reference data by category (Redis cached, TTL 1 hour)")
    public ResponseEntity<ApiResponse<List<RefDataResponse>>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(refDataService.getByCategory(category)));
    }
}
