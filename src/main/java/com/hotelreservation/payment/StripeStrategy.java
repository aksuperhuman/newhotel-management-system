package com.hotelreservation.payment;

import com.hotelreservation.domain.enums.PaymentGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class StripeStrategy implements PaymentGatewayStrategy {
    @Override public PaymentGateway gateway() { return PaymentGateway.STRIPE; }

    @Override public PaymentResult charge(BigDecimal amount, String reference) {
        return ThreadLocalRandom.current().nextInt(100) < 92
                ? PaymentResult.ok("stripe_" + UUID.randomUUID())
                : PaymentResult.fail("Stripe payment failed");
    }

    @Override public PaymentResult refund(String transactionRef) {
        return PaymentResult.ok("stripe_refund_" + UUID.randomUUID());
    }
}
