package com.streamverse.repository;

import com.streamverse.entity.Earning;
import com.streamverse.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface EarningRepository extends JpaRepository<Earning, Long> {

    Page<Earning> findByCreatorOrderByEarnedAtDesc(User creator, Pageable pageable);

    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.creator = :creator")
    Double sumByCreator(@Param("creator") User creator);

    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.creator = :creator AND e.earnedAt >= :from")
    Double sumByCreatorAndEarnedAtAfter(@Param("creator") User creator, @Param("from") LocalDateTime from);
}
