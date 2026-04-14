package com.ahatravel.customer.repository;

import com.ahatravel.customer.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {

    List<BookingStatusHistory> findAllByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
