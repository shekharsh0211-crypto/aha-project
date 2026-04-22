package com.ahatravel.customer.service;

import com.ahatravel.customer.dto.request.BookingFilterRequest;
import com.ahatravel.customer.dto.request.CreateBookingRequest;
import com.ahatravel.customer.dto.response.BookingResponse;
import com.ahatravel.customer.dto.response.PageResponse;
import com.ahatravel.customer.entity.Booking;
import com.ahatravel.customer.entity.BookingStatusHistory;
import com.ahatravel.customer.entity.User;
import com.ahatravel.customer.entity.Vehicle;
import com.ahatravel.customer.enums.BookingStatus;
import com.ahatravel.customer.exception.BusinessException;
import com.ahatravel.customer.exception.ResourceNotFoundException;
import com.ahatravel.customer.repository.BookingRepository;
import com.ahatravel.customer.repository.BookingSpecification;
import com.ahatravel.customer.repository.BookingStatusHistoryRepository;
import com.ahatravel.customer.repository.UserRepository;
import com.ahatravel.customer.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final AtomicLong REF_COUNTER = new AtomicLong(1000);

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        } else if (request.getVehicleType() != null && !request.getVehicleType().isBlank()) {
            vehicle = vehicleRepository.findFirstByVehicleTypeIgnoreCaseAndActiveTrue(request.getVehicleType())
                    .orElse(null);
        }

        Booking booking = Booking.builder()
                .referenceNumber(generateReference())
                .user(user)
                .vehicle(vehicle)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .pickupDatetime(request.getPickupDatetime())
                .passengerCount(request.getPassengerCount())
                .passengerName(request.getPassengerName())
                .passengerPhone(request.getPassengerPhone())
                .specialInstructions(request.getSpecialInstructions())
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);
        recordStatusHistory(booking, null, BookingStatus.PENDING, "Booking created", getAuditor());

        log.info("Booking created: {} for user: {}", booking.getReferenceNumber(), userId);
        return toResponse(booking, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> listBookings(BookingFilterRequest filter, Long userId, boolean isAdmin) {
        Pageable pageable = buildPageable(filter);

        Long filterUserId = isAdmin ? filter.getUserId() : userId;
        Specification<Booking> spec = Specification
                .where(BookingSpecification.forUser(filterUserId))
                .and(BookingSpecification.hasStatus(filter.getStatus()))
                .and(BookingSpecification.pickupAfter(filter.getFrom()))
                .and(BookingSpecification.pickupBefore(filter.getTo()));
        Page<Booking> page = bookingRepository.findAll(spec, pageable);

        return PageResponse.<BookingResponse>builder()
                .content(page.getContent().stream().map(b -> toResponse(b, false)).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, Long userId, boolean isAdmin) {
        Booking booking;
        if (isAdmin) {
            booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        } else {
            booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        }
        return toResponse(booking, true);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String reason, Long userId, boolean isAdmin) {
        Booking booking = isAdmin
                ? bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId))
                : bookingRepository.findByIdAndUserId(bookingId, userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed booking");
        }

        BookingStatus prevStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        bookingRepository.save(booking);

        recordStatusHistory(booking, prevStatus, BookingStatus.CANCELLED, reason, getAuditor());
        log.info("Booking {} cancelled by user {}", booking.getReferenceNumber(), userId);
        return toResponse(booking, true);
    }

    @Transactional
    public BookingResponse rebookBooking(Long bookingId, CreateBookingRequest request,
                                         Long userId, boolean isAdmin) {
        Booking original = isAdmin
                ? bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId))
                : bookingRepository.findByIdAndUserId(bookingId, userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (original.getStatus() != BookingStatus.CANCELLED
                && original.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("Only cancelled or completed bookings can be rebooked",
                    HttpStatus.CONFLICT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        }

        Booking newBooking = Booking.builder()
                .referenceNumber(generateReference())
                .user(user)
                .vehicle(vehicle)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .pickupDatetime(request.getPickupDatetime())
                .passengerCount(request.getPassengerCount())
                .specialInstructions(request.getSpecialInstructions())
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : original.getCurrency())
                .status(BookingStatus.PENDING)
                .rebookedFrom(original)
                .build();

        original.setStatus(BookingStatus.REBOOKED);
        bookingRepository.save(original);
        recordStatusHistory(original, original.getStatus(), BookingStatus.REBOOKED,
                "Rebooked as new booking", getAuditor());

        newBooking = bookingRepository.save(newBooking);
        recordStatusHistory(newBooking, null, BookingStatus.PENDING,
                "Rebooked from #" + original.getReferenceNumber(), getAuditor());

        return toResponse(newBooking, true);
    }

    private void recordStatusHistory(Booking booking, BookingStatus from, BookingStatus to,
                                      String remarks, String changedBy) {
        BookingStatusHistory history = BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(from)
                .toStatus(to)
                .remarks(remarks)
                .changedBy(changedBy)
                .build();
        historyRepository.save(history);
    }

    private BookingResponse toResponse(Booking b, boolean includeHistory) {
        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(b.getId())
                .referenceNumber(b.getReferenceNumber())
                .status(b.getStatus())
                .pickupLocation(b.getPickupLocation())
                .dropoffLocation(b.getDropoffLocation())
                .pickupDatetime(b.getPickupDatetime())
                .dropoffDatetime(b.getDropoffDatetime())
                .passengerCount(b.getPassengerCount())
                .passengerName(b.getPassengerName())
                .passengerPhone(b.getPassengerPhone())
                .specialInstructions(b.getSpecialInstructions())
                .totalAmount(b.getTotalAmount())
                .currency(b.getCurrency())
                .cancellationReason(b.getCancellationReason())
                .userId(b.getUser().getId())
                .userFullName(b.getUser().getFullName())
                .rebookedFromId(b.getRebookedFrom() != null ? b.getRebookedFrom().getId() : null)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt());

        if (b.getVehicle() != null) {
            builder.vehicleId(b.getVehicle().getId())
                    .vehiclePlate(b.getVehicle().getPlateNumber())
                    .vehicleDescription(b.getVehicle().getMake() + " " + b.getVehicle().getModel());
        }

        if (includeHistory) {
            List<BookingStatusHistory> history =
                    historyRepository.findAllByBookingIdOrderByCreatedAtAsc(b.getId());
            builder.statusHistory(history.stream().map(h ->
                    BookingResponse.StatusHistoryResponse.builder()
                            .id(h.getId())
                            .fromStatus(h.getFromStatus())
                            .toStatus(h.getToStatus())
                            .changedBy(h.getChangedBy())
                            .remarks(h.getRemarks())
                            .createdAt(h.getCreatedAt())
                            .build()).toList());
        }
        return builder.build();
    }

    private Pageable buildPageable(BookingFilterRequest filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    private String generateReference() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "BK-" + date + "-" + REF_COUNTER.getAndIncrement();
    }

    private String getAuditor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
