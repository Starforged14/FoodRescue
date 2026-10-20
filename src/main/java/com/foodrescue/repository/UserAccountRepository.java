package com.foodrescue.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodrescue.entity.AppRole;
import com.foodrescue.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        select count(distinct user.id) from UserAccount user
        join user.roles accountRole
        where accountRole = :role
        """)
    long countUsersByRole(@Param("role") AppRole role);
}
