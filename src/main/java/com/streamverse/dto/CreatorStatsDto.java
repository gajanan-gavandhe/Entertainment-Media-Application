package com.streamverse.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreatorStatsDto {
    private long totalViews;
    private double totalEarnings;
    private double monthEarnings;
    private double weekEarnings;
    private long contentCount;
}
