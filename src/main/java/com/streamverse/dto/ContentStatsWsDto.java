package com.streamverse.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContentStatsWsDto {
    private Long contentId;
    private Long viewCount;
    private Long playCount;
    private Double averageRating;
}
