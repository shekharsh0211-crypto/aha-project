package com.ahatravel.customer.repository;

import com.ahatravel.customer.entity.User;
import com.ahatravel.customer.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndActiveTrue(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAllByActiveTrue(Pageable pageable);

    Page<User> findAllByRoleAndActiveTrue(UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.active = true AND " +
            "(:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchActiveUsers(@Param("search") String search, Pageable pageable);
}
