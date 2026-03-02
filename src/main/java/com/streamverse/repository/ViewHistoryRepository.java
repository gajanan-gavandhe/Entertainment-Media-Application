package com.streamverse.repository;

import com.streamverse.entity.User;
import com.streamverse.entity.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {
    long countByUser(User user);
}
