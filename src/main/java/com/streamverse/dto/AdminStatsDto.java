package com.streamverse.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminStatsDto {
    private long totalUsers;
    private long activeUsers;
    private long newUsersThisMonth;
    private long publishedContent;
    private long totalContent;
    private long pendingContent;
    private double totalRevenue;
    private double monthRevenue;
}
