package com.streamverse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    private String genre;
    private String mediaUrl;
    private String ageRating;
    private String duration;
    private String castMembers;
    private Integer releaseYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    private MonetizationType monetizationType = MonetizationType.FREE;

    private Double price;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long playCount = 0L;

    @Column(nullable = false, columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double averageRating = 0.0;

    @Column(nullable = false, columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double totalEarnings = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (viewCount == null) viewCount = 0L;
        if (playCount == null) playCount = 0L;
        if (averageRating == null) averageRating = 0.0;
        if (totalEarnings == null) totalEarnings = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ContentType {
        MOVIE, SERIES, MUSIC, PODCAST, LIVE, ORIGINAL
    }

    public enum ContentStatus {
        DRAFT, PENDING_REVIEW, PUBLISHED, REJECTED, ARCHIVED
    }

    public enum MonetizationType {
        FREE, SUBSCRIPTION, PAY_PER_VIEW, RENTAL, PREMIUM
    }
}
