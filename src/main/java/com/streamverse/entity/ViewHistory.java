package com.streamverse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "view_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    private LocalDateTime viewedAt;

    @PrePersist
    protected void onCreate() {
        viewedAt = LocalDateTime.now();
    }
}
