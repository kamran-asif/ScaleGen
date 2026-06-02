package com.genai.paymentservice.controller;

import com.genai.paymentservice.dto.CreatePaymentRequest;
import com.genai.paymentservice.dto.CreatePaymentResponse;
import com.genai.paymentservice.entity.Payment;
import com.genai.paymentservice.entity.UserBalance;
import com.genai.paymentservice.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<CreatePaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            CreatePaymentResponse response = paymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (StripeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/webhook/succeeded/{paymentIntentId}")
    public ResponseEntity<Void> handlePaymentSucceeded(@PathVariable String paymentIntentId) {
        paymentService.handlePaymentSucceeded(paymentIntentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<UserBalance> getUserBalance(@PathVariable String userId) {
        return ResponseEntity.ok(paymentService.getUserBalance(userId));
    }

    @PostMapping("/use-credits")
    public ResponseEntity<Map<String, Boolean>> useCredits(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        Integer credits = (Integer) request.get("credits");
        boolean success = paymentService.useCredits(userId, credits);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable String userId) {
        return ResponseEntity.ok(paymentService.getUserPayments(userId));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId) {
        Payment payment = paymentService.getPayment(paymentId);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }
}
