package com.hotelreservation.payment;

import com.hotelreservation.domain.enums.PaymentGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RazorpayStrategy implements PaymentGatewayStrategy {
    @Override public PaymentGateway gateway() { return PaymentGateway.RAZORPAY; }

    @Override public PaymentResult charge(BigDecimal amount, String reference) {
        // Simulate a gateway call with ~90% success rate.
        return ThreadLocalRandom.current().nextInt(100) < 90
                ? PaymentResult.ok("rzp_" + UUID.randomUUID())
                : PaymentResult.fail("Razorpay declined the card");
    }

    @Override public PaymentResult refund(String transactionRef) {
        return PaymentResult.ok("rzp_refund_" + UUID.randomUUID());
    }
}
