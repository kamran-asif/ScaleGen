package com.genai.paymentservice.service;

import com.genai.paymentservice.dto.CreatePaymentRequest;
import com.genai.paymentservice.dto.CreatePaymentResponse;
import com.genai.paymentservice.entity.Payment;
import com.genai.paymentservice.entity.UserBalance;
import com.genai.paymentservice.repository.PaymentRepository;
import com.genai.paymentservice.repository.UserBalanceRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserBalanceRepository userBalanceRepository;

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "usd")
                .status(Payment.PaymentStatus.PENDING)
                .product(request.getProduct())
                .credits(request.getCredits())
                .description(request.getDescription())
                .build();

        payment = paymentRepository.save(payment);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.getAmount().longValue())
                .setCurrency(request.getCurrency() != null ? request.getCurrency() : "usd")
                .putMetadata("paymentId", payment.getId())
                .putMetadata("userId", request.getUserId())
                .putMetadata("credits", request.getCredits().toString())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        payment.setStripePaymentIntentId(paymentIntent.getId());
        paymentRepository.save(payment);

        return CreatePaymentResponse.builder()
                .paymentId(payment.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .status(payment.getStatus().name())
                .build();
    }

    public void handleWebhook(String payload, String sigHeader) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        com.stripe.net.Webhook.constructEvent(payload, sigHeader, stripeSecretKey);
    }

    public void handlePaymentSucceeded(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId);
        if (payment != null && payment.getStatus() == Payment.PaymentStatus.PENDING) {
            payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);

            addCreditsToUser(payment.getUserId(), payment.getCredits());
            log.info("Payment succeeded for user: {}, credits added: {}", payment.getUserId(), payment.getCredits());
        }
    }

    public void handlePaymentFailed(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId);
        if (payment != null) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.info("Payment failed for user: {}", payment.getUserId());
        }
    }

    private void addCreditsToUser(String userId, Integer credits) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElseGet(() -> UserBalance.builder()
                        .userId(userId)
                        .credits(0)
                        .build());

        userBalance.setCredits(userBalance.getCredits() + credits);
        userBalanceRepository.save(userBalance);
    }

    public UserBalance getUserBalance(String userId) {
        return userBalanceRepository.findByUserId(userId)
                .orElseGet(() -> UserBalance.builder()
                        .userId(userId)
                        .credits(0)
                        .build());
    }

    public boolean useCredits(String userId, int creditsToUse) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId).orElse(null);
        if (userBalance == null || userBalance.getCredits() < creditsToUse) {
            return false;
        }

        userBalance.setCredits(userBalance.getCredits() - creditsToUse);
        userBalanceRepository.save(userBalance);
        return true;
    }

    public List<Payment> getUserPayments(String userId) {
        return paymentRepository.findByUserId(userId);
    }

    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }
}
