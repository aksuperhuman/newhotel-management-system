package com.hotelreservation.payment;

import com.hotelreservation.domain.enums.PaymentGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PaypalStrategy implements PaymentGatewayStrategy {
    @Override public PaymentGateway gateway() { return PaymentGateway.PAYPAL; }

    @Override public PaymentResult charge(BigDecimal amount, String reference) {
        return ThreadLocalRandom.current().nextInt(100) < 88
                ? PaymentResult.ok("pp_" + UUID.randomUUID())
                : PaymentResult.fail("PayPal transaction rejected");
    }

    @Override public PaymentResult refund(String transactionRef) {
        return PaymentResult.ok("pp_refund_" + UUID.randomUUID());
    }
}
