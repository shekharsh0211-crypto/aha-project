package com.ahatravel.customer.repository;

import com.ahatravel.customer.entity.FeedbackSupport;
import com.ahatravel.customer.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackSupportRepository extends JpaRepository<FeedbackSupport, Long> {

    Page<FeedbackSupport> findAllByUserIdAndParentIsNull(Long userId, Pageable pageable);

    Page<FeedbackSupport> findAllByParentIsNull(Pageable pageable);

    List<FeedbackSupport> findAllByParentId(Long parentId);

    Page<FeedbackSupport> findAllByTicketStatusAndParentIsNull(TicketStatus status, Pageable pageable);

    Page<FeedbackSupport> findAllByBookingIdAndParentIsNull(Long bookingId, Pageable pageable);
}
