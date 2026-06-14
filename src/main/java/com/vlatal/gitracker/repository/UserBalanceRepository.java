package com.vlatal.gitracker.repository;

import com.vlatal.gitracker.entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    Optional<UserBalance> findByUserId(String userId);
}
