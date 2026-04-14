package com.ahatravel.customer.repository;

import com.ahatravel.customer.entity.Booking;
import com.ahatravel.customer.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    Optional<Booking> findByReferenceNumber(String referenceNumber);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    Page<Booking> findAllByUserId(Long userId, Pageable pageable);

    // Dashboard counts
    long countByStatus(BookingStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.createdAt >= :since")
    long countRecentByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :since")
    long countRecentAll(@Param("since") LocalDateTime since);
}
