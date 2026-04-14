package com.ahatravel.customer.service;

import com.ahatravel.customer.dto.response.DashboardResponse;
import com.ahatravel.customer.enums.BookingStatus;
import com.ahatravel.customer.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getSummary(Long userId, boolean isAdmin) {
        if (isAdmin) {
            return DashboardResponse.builder()
                    .totalBookings(bookingRepository.count())
                    .pendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING))
                    .confirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED))
                    .inProgressBookings(bookingRepository.countByStatus(BookingStatus.IN_PROGRESS))
                    .completedBookings(bookingRepository.countByStatus(BookingStatus.COMPLETED))
                    .cancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED))
                    .recentBookings(bookingRepository.countRecentAll(LocalDateTime.now().minusDays(30)))
                    .build();
        } else {
            return DashboardResponse.builder()
                    .totalBookings(bookingRepository.countByUserId(userId))
                    .pendingBookings(bookingRepository.countByUserIdAndStatus(userId, BookingStatus.PENDING))
                    .confirmedBookings(bookingRepository.countByUserIdAndStatus(userId, BookingStatus.CONFIRMED))
                    .inProgressBookings(bookingRepository.countByUserIdAndStatus(userId, BookingStatus.IN_PROGRESS))
                    .completedBookings(bookingRepository.countByUserIdAndStatus(userId, BookingStatus.COMPLETED))
                    .cancelledBookings(bookingRepository.countByUserIdAndStatus(userId, BookingStatus.CANCELLED))
                    .recentBookings(bookingRepository.countRecentByUserId(userId, LocalDateTime.now().minusDays(30)))
                    .build();
        }
    }

}
