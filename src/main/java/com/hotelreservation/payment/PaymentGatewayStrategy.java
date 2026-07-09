package com.hotelreservation.payment;

import com.hotelreservation.domain.enums.PaymentGateway;

import java.math.BigDecimal;

/**
 * Strategy pattern: one implementation per payment provider. The engine picks a
 * strategy at runtime by gateway, so adding a provider never touches call sites.
 */
public interface PaymentGatewayStrategy {
    PaymentGateway gateway();
    PaymentResult charge(BigDecimal amount, String reference);
    PaymentResult refund(String transactionRef);
}
