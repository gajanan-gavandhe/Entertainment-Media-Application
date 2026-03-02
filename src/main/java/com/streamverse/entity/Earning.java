package com.streamverse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "earnings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Earning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    private EarningType type = EarningType.VIEW;

    private LocalDateTime earnedAt;

    @PrePersist
    protected void onCreate() {
        earnedAt = LocalDateTime.now();
    }

    public enum EarningType {
        VIEW, PLAY, SUBSCRIPTION, PAY_PER_VIEW, RENTAL
    }
}
