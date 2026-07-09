package com.hotel.reservation.payment;

import com.hotel.reservation.domain.PaymentProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class StripeGateway implements PaymentGateway {

    @Override public PaymentProvider provider() { return PaymentProvider.STRIPE; }

    @Override
    public PaymentResult charge(String reference, BigDecimal amount) {
        boolean ok = ThreadLocalRandom.current().nextInt(100) < 92;
        return new PaymentResult(ok, ok ? "pi_" + UUID.randomUUID() : null,
                ok ? "succeeded" : "insufficient_funds");
    }

    @Override
    public PaymentResult refund(String providerRef, BigDecimal amount) {
        return new PaymentResult(true, providerRef, "refunded");
    }
}
