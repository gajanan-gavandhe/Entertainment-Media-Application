package com.streamverse.dto;
import com.streamverse.entity.Content;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContentDto {
    private Long id;
    private String title;
    private String description;
    private Content.ContentType type;
    private String genre;
    private String mediaUrl;
    private String ageRating;
    private String duration;
    private String castMembers;
    private Integer releaseYear;
    private Content.ContentStatus status;
    private Content.MonetizationType monetizationType;
    private Double price;
    private Long viewCount;
    private Long playCount;
    private Double averageRating;
    private Double totalEarnings;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public static ContentDto from(Content c) {
        return ContentDto.builder()
            .id(c.getId()).title(c.getTitle()).description(c.getDescription())
            .type(c.getType()).genre(c.getGenre()).mediaUrl(c.getMediaUrl())
            .ageRating(c.getAgeRating()).duration(c.getDuration())
            .castMembers(c.getCastMembers()).releaseYear(c.getReleaseYear())
            .status(c.getStatus()).monetizationType(c.getMonetizationType())
            .price(c.getPrice()).viewCount(c.getViewCount()).playCount(c.getPlayCount())
            .averageRating(c.getAverageRating()).totalEarnings(c.getTotalEarnings())
            .creatorId(c.getCreator() != null ? c.getCreator().getId() : null)
            .creatorName(c.getCreator() != null ? c.getCreator().getFullName() : null)
            .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }
}
