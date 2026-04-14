package com.ahatravel.customer.service;

import com.ahatravel.customer.dto.request.CreateFeedbackRequest;
import com.ahatravel.customer.dto.request.ReplyRequest;
import com.ahatravel.customer.dto.response.FeedbackResponse;
import com.ahatravel.customer.dto.response.PageResponse;
import com.ahatravel.customer.entity.Booking;
import com.ahatravel.customer.entity.FeedbackSupport;
import com.ahatravel.customer.entity.User;
import com.ahatravel.customer.enums.TicketStatus;
import com.ahatravel.customer.exception.BusinessException;
import com.ahatravel.customer.exception.ResourceNotFoundException;
import com.ahatravel.customer.repository.BookingRepository;
import com.ahatravel.customer.repository.FeedbackSupportRepository;
import com.ahatravel.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackSupportRepository feedbackRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public FeedbackResponse createTicket(CreateFeedbackRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));
        }

        FeedbackSupport ticket = FeedbackSupport.builder()
                .user(user)
                .booking(booking)
                .subject(request.getSubject())
                .message(request.getMessage())
                .rating(request.getRating())
                .isFeedback(Boolean.TRUE.equals(request.getIsFeedback()))
                .ticketStatus(TicketStatus.OPEN)
                .build();

        return toResponse(feedbackRepository.save(ticket), false);
    }

    @Transactional
    public FeedbackResponse addReply(Long ticketId, ReplyRequest request, Long userId) {
        FeedbackSupport parent = feedbackRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (parent.getParent() != null) {
            throw new BusinessException("Cannot reply to a reply; only top-level tickets can have replies");
        }

        if (parent.getTicketStatus() == TicketStatus.CLOSED) {
            throw new BusinessException("Cannot reply to a closed ticket", HttpStatus.CONFLICT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        FeedbackSupport reply = FeedbackSupport.builder()
                .user(user)
                .parent(parent)
                .subject("RE: " + parent.getSubject())
                .message(request.getMessage())
                .ticketStatus(TicketStatus.OPEN)
                .isFeedback(false)
                .build();

        // Update parent status to in-progress
        if (parent.getTicketStatus() == TicketStatus.OPEN) {
            parent.setTicketStatus(TicketStatus.IN_PROGRESS);
            feedbackRepository.save(parent);
        }

        return toResponse(feedbackRepository.save(reply), false);
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedbackResponse> listTickets(Long userId, boolean isAdmin,
                                                       TicketStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<FeedbackSupport> result;
        if (isAdmin) {
            result = status != null
                    ? feedbackRepository.findAllByTicketStatusAndParentIsNull(status, pageable)
                    : feedbackRepository.findAllByParentIsNull(pageable);
        } else {
            result = feedbackRepository.findAllByUserIdAndParentIsNull(userId, pageable);
        }

        return PageResponse.<FeedbackResponse>builder()
                .content(result.getContent().stream().map(f -> toResponse(f, true)).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getTicket(Long ticketId, Long userId, boolean isAdmin) {
        FeedbackSupport ticket = feedbackRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
        if (!isAdmin && !ticket.getUser().getId().equals(userId)) {
            throw new BusinessException("Access denied to this ticket", HttpStatus.FORBIDDEN);
        }
        return toResponse(ticket, true);
    }

    @Transactional
    public FeedbackResponse closeTicket(Long ticketId, Long userId, boolean isAdmin) {
        FeedbackSupport ticket = feedbackRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
        if (!isAdmin && !ticket.getUser().getId().equals(userId)) {
            throw new BusinessException("Access denied to this ticket", HttpStatus.FORBIDDEN);
        }
        ticket.setTicketStatus(TicketStatus.CLOSED);
        return toResponse(feedbackRepository.save(ticket), true);
    }

    private FeedbackResponse toResponse(FeedbackSupport f, boolean includeReplies) {
        FeedbackResponse.FeedbackResponseBuilder builder = FeedbackResponse.builder()
                .id(f.getId())
                .userId(f.getUser().getId())
                .userFullName(f.getUser().getFullName())
                .subject(f.getSubject())
                .message(f.getMessage())
                .rating(f.getRating())
                .ticketStatus(f.getTicketStatus())
                .isFeedback(f.getIsFeedback())
                .parentId(f.getParent() != null ? f.getParent().getId() : null)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt());

        if (f.getBooking() != null) {
            builder.bookingId(f.getBooking().getId())
                    .bookingReference(f.getBooking().getReferenceNumber());
        }

        if (includeReplies && f.getReplies() != null) {
            List<FeedbackResponse> replies = f.getReplies().stream()
                    .map(r -> toResponse(r, false))
                    .toList();
            builder.replies(replies);
        }

        return builder.build();
    }
}
