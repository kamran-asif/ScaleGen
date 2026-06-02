package com.genai.paymentservice.repository;

import com.genai.paymentservice.entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, String> {

    Optional<UserBalance> findByUserId(String userId);
}
