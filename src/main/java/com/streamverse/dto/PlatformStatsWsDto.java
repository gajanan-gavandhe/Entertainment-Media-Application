package com.streamverse.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PlatformStatsWsDto {
    private long totalUsers;
    private long totalContent;
    private double totalRevenue;
    private double monthRevenue;
    private long pendingContent;
}
