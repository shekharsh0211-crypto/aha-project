package com.ahatravel.customer.controller;

import com.ahatravel.customer.dto.request.CreateUserRequest;
import com.ahatravel.customer.dto.response.ApiResponse;
import com.ahatravel.customer.dto.response.PageResponse;
import com.ahatravel.customer.dto.response.UserResponse;
import com.ahatravel.customer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - User Management", description = "Admin operations for managing users")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", userService.createUser(request)));
    }

    @GetMapping
    @Operation(summary = "List all users with search and pagination (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.listUsers(search, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle user active status (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User status updated",
                userService.toggleUserStatus(id)));
    }
}
