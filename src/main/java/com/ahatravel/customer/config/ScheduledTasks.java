package com.ahatravel.customer.config;

import com.ahatravel.customer.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final UserSessionRepository sessionRepository;

    /**
     * Purge expired / revoked sessions every night at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void purgeExpiredSessions() {
        log.info("Running expired session cleanup...");
        sessionRepository.deleteExpiredAndRevoked(LocalDateTime.now());
        log.info("Expired session cleanup complete.");
    }
}
