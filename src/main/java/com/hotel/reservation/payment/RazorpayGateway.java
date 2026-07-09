package com.hotel.reservation.payment;

import com.hotel.reservation.domain.PaymentProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Simulated Razorpay gateway. ~90% success rate to exercise failure paths. */
@Component
public class RazorpayGateway implements PaymentGateway {

    @Override public PaymentProvider provider() { return PaymentProvider.RAZORPAY; }

    @Override
    public PaymentResult charge(String reference, BigDecimal amount) {
        boolean ok = ThreadLocalRandom.current().nextInt(100) < 90;
        return new PaymentResult(ok, ok ? "rzp_" + UUID.randomUUID() : null,
                ok ? "Captured" : "Card declined");
    }

    @Override
    public PaymentResult refund(String providerRef, BigDecimal amount) {
        return new PaymentResult(true, providerRef, "Refunded");
    }
}
