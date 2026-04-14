package com.ahatravel.customer.controller;

import com.ahatravel.customer.dto.request.CreateFeedbackRequest;
import com.ahatravel.customer.dto.request.ReplyRequest;
import com.ahatravel.customer.dto.response.ApiResponse;
import com.ahatravel.customer.dto.response.FeedbackResponse;
import com.ahatravel.customer.dto.response.PageResponse;
import com.ahatravel.customer.enums.TicketStatus;
import com.ahatravel.customer.enums.UserRole;
import com.ahatravel.customer.security.UserPrincipal;
import com.ahatravel.customer.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Feedback & Support", description = "Feedback, rating, and support tickets")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Create a support ticket or feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createTicket(
            @Valid @RequestBody CreateFeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created",
                        feedbackService.createTicket(request, principal.getId())));
    }

    @GetMapping
    @Operation(summary = "List tickets (own for customer, all for admin)")
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> listTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = isAdmin(principal);
        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.listTickets(principal.getId(), isAdmin, status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket detail with replies")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = isAdmin(principal);
        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.getTicket(id, principal.getId(), isAdmin)));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "Add a reply to a ticket")
    public ResponseEntity<ApiResponse<FeedbackResponse>> addReply(
            @PathVariable Long id,
            @Valid @RequestBody ReplyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reply added",
                        feedbackService.addReply(id, request, principal.getId())));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close a ticket")
    public ResponseEntity<ApiResponse<FeedbackResponse>> closeTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = isAdmin(principal);
        return ResponseEntity.ok(ApiResponse.success("Ticket closed",
                feedbackService.closeTicket(id, principal.getId(), isAdmin)));
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));
    }
}
