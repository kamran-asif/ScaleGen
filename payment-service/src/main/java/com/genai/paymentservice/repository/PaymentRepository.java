package com.genai.paymentservice.repository;

import com.genai.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByUserId(String userId);

    Payment findByStripePaymentIntentId(String stripePaymentIntentId);
}
