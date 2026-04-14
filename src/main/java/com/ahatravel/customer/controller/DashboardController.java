package com.ahatravel.customer.controller;

import com.ahatravel.customer.dto.response.ApiResponse;
import com.ahatravel.customer.dto.response.DashboardResponse;
import com.ahatravel.customer.enums.UserRole;
import com.ahatravel.customer.security.UserPrincipal;
import com.ahatravel.customer.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Summary statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get booking summary counts")
    public ResponseEntity<ApiResponse<DashboardResponse>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getSummary(principal.getId(), isAdmin)));
    }
}
