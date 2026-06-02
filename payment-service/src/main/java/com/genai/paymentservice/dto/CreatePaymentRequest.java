package com.genai.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private String userId;
    private String product;
    private BigDecimal amount;
    private String currency;
    private Integer credits;
    private String description;
}
