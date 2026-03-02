package com.streamverse.repository;

import com.streamverse.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:q IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<User> findWithFilters(
        @Param("role") User.Role role,
        @Param("status") User.UserStatus status,
        @Param("q") String q,
        Pageable pageable
    );

    long countByCreatedAtAfter(LocalDateTime date);
    long countByStatus(User.UserStatus status);
}
