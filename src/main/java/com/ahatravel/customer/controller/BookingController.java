package com.ahatravel.customer.controller;

import com.ahatravel.customer.dto.request.BookingFilterRequest;
import com.ahatravel.customer.dto.request.CreateBookingRequest;
import com.ahatravel.customer.dto.response.ApiResponse;
import com.ahatravel.customer.dto.response.BookingResponse;
import com.ahatravel.customer.dto.response.PageResponse;
import com.ahatravel.customer.enums.BookingStatus;
import com.ahatravel.customer.enums.UserRole;
import com.ahatravel.customer.security.UserPrincipal;
import com.ahatravel.customer.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bookings", description = "Booking management")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse response = bookingService.createBooking(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List bookings with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> listBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));

        BookingFilterRequest filter = new BookingFilterRequest();
        filter.setStatus(status);
        filter.setFrom(from);
        filter.setTo(to);
        filter.setUserId(userId);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDir(sortDir);

        return ResponseEntity.ok(ApiResponse.success(
                bookingService.listBookings(filter, principal.getId(), isAdmin)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking detail")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = isAdmin(principal);
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getBooking(id, principal.getId(), isAdmin)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Customer requested cancellation") String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = isAdmin(principal);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled",
                bookingService.cancelBooking(id, reason, principal.getId(), isAdmin)));
    }

    @PostMapping("/{id}/rebook")
    @Operation(summary = "Rebook a cancelled or completed booking")
    public ResponseEntity<ApiResponse<BookingResponse>> rebookBooking(
            @PathVariable Long id,
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = isAdmin(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking rebooked",
                        bookingService.rebookBooking(id, request, principal.getId(), isAdmin)));
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
    }
}
