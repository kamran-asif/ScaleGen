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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final UserBalanceRepository userBalanceRepository;

    @Value("${stripe.api.secret-key:sk_test_mock}")
    private String stripeSecretKey;

    public PaymentService(PaymentRepository paymentRepository, UserBalanceRepository userBalanceRepository) {
        this.paymentRepository = paymentRepository;
        this.userBalanceRepository = userBalanceRepository;
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws StripeException {
        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "usd")
                .status(Payment.PaymentStatus.PENDING)
                .credits(request.getCredits())
                .build();

        payment = paymentRepository.save(payment);

        String clientSecret = "mock_secret_" + payment.getId();
        if (stripeSecretKey != null && !stripeSecretKey.contains("mock") && !stripeSecretKey.contains("your_secret")) {
            try {
                Stripe.apiKey = stripeSecretKey;
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(request.getAmount().multiply(new BigDecimal(100)).longValue())
                        .setCurrency(request.getCurrency() != null ? request.getCurrency() : "usd")
                        .putMetadata("paymentId", payment.getId())
                        .putMetadata("userId", request.getUserId())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

                PaymentIntent paymentIntent = PaymentIntent.create(params);
                clientSecret = paymentIntent.getClientSecret();
                payment.setStripePaymentIntentId(paymentIntent.getId());
                paymentRepository.save(payment);
            } catch (Exception e) {
                log.warn("[Stripe] Stripe call failed or unconfigured, using mock secret: {}", e.getMessage());
            }
        }

        return CreatePaymentResponse.builder()
                .paymentId(payment.getId())
                .clientSecret(clientSecret)
                .status(payment.getStatus().name())
                .build();
    }

    public void handleWebhook(String payload, String sigHeader) throws StripeException {
        if (stripeSecretKey != null && !stripeSecretKey.contains("mock")) {
            Stripe.apiKey = stripeSecretKey;
            com.stripe.net.Webhook.constructEvent(payload, sigHeader, stripeSecretKey);
        }
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
                        .credits(100)
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
