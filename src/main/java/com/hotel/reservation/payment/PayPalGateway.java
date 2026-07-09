package com.hotel.reservation.payment;

import com.hotel.reservation.domain.PaymentProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PayPalGateway implements PaymentGateway {

    @Override public PaymentProvider provider() { return PaymentProvider.PAYPAL; }

    @Override
    public PaymentResult charge(String reference, BigDecimal amount) {
        boolean ok = ThreadLocalRandom.current().nextInt(100) < 88;
        return new PaymentResult(ok, ok ? "PAYID-" + UUID.randomUUID() : null,
                ok ? "COMPLETED" : "PAYER_CANNOT_PAY");
    }

    @Override
    public PaymentResult refund(String providerRef, BigDecimal amount) {
        return new PaymentResult(true, providerRef, "COMPLETED");
    }
}
