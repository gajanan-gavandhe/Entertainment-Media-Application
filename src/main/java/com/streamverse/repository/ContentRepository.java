package com.streamverse.repository;

import com.streamverse.entity.Content;
import com.streamverse.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    Page<Content> findByCreator(User creator, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.status = 'PUBLISHED' AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:genre IS NULL OR c.genre = :genre) AND " +
           "(:q IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Content> findPublishedWithFilters(
        @Param("type") Content.ContentType type,
        @Param("genre") String genre,
        @Param("q") String q,
        Pageable pageable
    );

    @Query("SELECT c FROM Content c WHERE " +
           "(:status IS NULL OR c.status = :status)")
    Page<Content> findAllWithStatusFilter(
        @Param("status") Content.ContentStatus status,
        Pageable pageable
    );

    long countByStatus(Content.ContentStatus status);
    long countByCreator(User creator);

    @Query("SELECT SUM(c.totalEarnings) FROM Content c WHERE c.creator = :creator")
    Double sumEarningsByCreator(@Param("creator") User creator);

    @Query("SELECT SUM(c.viewCount) FROM Content c WHERE c.creator = :creator")
    Long sumViewsByCreator(@Param("creator") User creator);

    @Query("SELECT SUM(c.totalEarnings) FROM Content c")
    Double sumTotalRevenue();

    @Query("SELECT SUM(c.totalEarnings) FROM Content c WHERE c.createdAt >= :from")
    Double sumRevenueFrom(@Param("from") LocalDateTime from);

    List<Content> findTop12ByStatusOrderByViewCountDesc(Content.ContentStatus status);
}
