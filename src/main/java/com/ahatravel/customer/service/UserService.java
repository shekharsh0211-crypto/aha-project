package com.ahatravel.customer.service;

import com.ahatravel.customer.dto.request.ChangePasswordRequest;
import com.ahatravel.customer.dto.request.CreateUserRequest;
import com.ahatravel.customer.dto.request.UpdateProfileRequest;
import com.ahatravel.customer.dto.response.PageResponse;
import com.ahatravel.customer.dto.response.UserResponse;
import com.ahatravel.customer.entity.Company;
import com.ahatravel.customer.entity.User;
import com.ahatravel.customer.exception.BusinessException;
import com.ahatravel.customer.exception.ResourceNotFoundException;
import com.ahatravel.customer.repository.CompanyRepository;
import com.ahatravel.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "userProfiles", key = "#userId")
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @CacheEvict(value = "userProfiles", key = "#userId")
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        return toResponse(userRepository.save(user));
    }

    @CacheEvict(value = "userProfiles", key = "#userId")
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    // Admin operations

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered: " + request.getEmail());
        }

        Company company = null;
        if (request.getCompanyId() != null) {
            company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .company(company)
                .active(true)
                .emailVerified(false)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(String search, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = search != null && !search.isBlank()
                ? userRepository.searchActiveUsers(search, pageable)
                : userRepository.findAllByActiveTrue(pageable);

        return PageResponse.<UserResponse>builder()
                .content(users.getContent().stream().map(this::toResponse).toList())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @CacheEvict(value = "userProfiles", key = "#userId")
    @Transactional
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        return toResponse(userRepository.save(user));
    }

    UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
