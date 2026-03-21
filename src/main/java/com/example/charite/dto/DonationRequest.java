package com.example.charite.dto;

import com.example.charite.enums.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DonationRequest {
    private BigDecimal amount;
    private String message;
    private PaymentMethod paymentMethod;
}