package com.ahatravel.customer.entity;

import com.ahatravel.customer.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feedback_support", indexes = {
        @Index(name = "idx_fs_booking_id", columnList = "booking_id"),
        @Index(name = "idx_fs_user_id", columnList = "user_id"),
        @Index(name = "idx_fs_status", columnList = "ticket_status"),
        @Index(name = "idx_fs_parent_id", columnList = "parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackSupport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "subject", nullable = false, length = 300)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "rating")
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false, length = 30)
    @Builder.Default
    private TicketStatus ticketStatus = TicketStatus.OPEN;

    @Column(name = "is_feedback", nullable = false)
    @Builder.Default
    private Boolean isFeedback = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FeedbackSupport parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FeedbackSupport> replies = new ArrayList<>();
}
