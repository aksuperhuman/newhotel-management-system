package com.hotel.reservation;

import com.hotel.reservation.domain.PaymentProvider;
import com.hotel.reservation.payment.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentGatewayFactoryTest {

    private final PaymentGatewayFactory factory = new PaymentGatewayFactory(
        List.of(new RazorpayGateway(), new StripeGateway(), new PayPalGateway()));

    @Test
    void resolvesEachProvider() {
        assertThat(factory.resolve(PaymentProvider.RAZORPAY)).isInstanceOf(RazorpayGateway.class);
        assertThat(factory.resolve(PaymentProvider.STRIPE)).isInstanceOf(StripeGateway.class);
        assertThat(factory.resolve(PaymentProvider.PAYPAL)).isInstanceOf(PayPalGateway.class);
    }

    @Test
    void chargeProducesResult() {
        var result = factory.resolve(PaymentProvider.STRIPE)
            .charge("ref-1", new java.math.BigDecimal("100.00"));
        assertThat(result).isNotNull();
    }
}
