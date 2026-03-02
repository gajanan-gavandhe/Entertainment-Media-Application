package com.streamverse.service;

import com.streamverse.dto.*;
import com.streamverse.entity.*;
import com.streamverse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final EarningRepository earningRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AdminStatsDto getAdminStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
        long newUsers = userRepository.countByCreatedAtAfter(LocalDateTime.now().minusMonths(1));
        long published = contentRepository.countByStatus(Content.ContentStatus.PUBLISHED);
        long pending = contentRepository.countByStatus(Content.ContentStatus.PENDING_REVIEW);
        long total = contentRepository.count();
        Double totalRev = contentRepository.sumTotalRevenue();
        Double monthRev = contentRepository.sumRevenueFrom(LocalDateTime.now().withDayOfMonth(1).withHour(0));
        return AdminStatsDto.builder()
            .totalUsers(totalUsers).activeUsers(activeUsers).newUsersThisMonth(newUsers)
            .publishedContent(published).totalContent(total).pendingContent(pending)
            .totalRevenue(totalRev != null ? totalRev : 0.0)
            .monthRevenue(monthRev != null ? monthRev : 0.0)
            .build();
    }

    public CreatorStatsDto getCreatorStats(User creator) {
        Long views = contentRepository.sumViewsByCreator(creator);
        Double total = contentRepository.sumEarningsByCreator(creator);
        Double month = earningRepository.sumByCreatorAndEarnedAtAfter(creator,
            LocalDateTime.now().withDayOfMonth(1).withHour(0));
        Double week = earningRepository.sumByCreatorAndEarnedAtAfter(creator,
            LocalDateTime.now().minusDays(7));
        long count = contentRepository.countByCreator(creator);
        return CreatorStatsDto.builder()
            .totalViews(views != null ? views : 0L)
            .totalEarnings(total != null ? total : 0.0)
            .monthEarnings(month != null ? month : 0.0)
            .weekEarnings(week != null ? week : 0.0)
            .contentCount(count)
            .build();
    }

    @Scheduled(fixedRate = 10000)
    public void broadcastPlatformStats() {
        try {
            AdminStatsDto stats = getAdminStats();
            PlatformStatsWsDto dto = PlatformStatsWsDto.builder()
                .totalUsers(stats.getTotalUsers())
                .totalContent(stats.getTotalContent())
                .totalRevenue(stats.getTotalRevenue())
                .monthRevenue(stats.getMonthRevenue())
                .pendingContent(stats.getPendingContent())
                .build();
            messagingTemplate.convertAndSend("/topic/platform-stats", dto);
        } catch (Exception ignored) {}
    }
}
