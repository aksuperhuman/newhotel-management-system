package com.hotelreservation.payment;

import com.hotelreservation.domain.enums.PaymentGateway;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentGatewayFactoryTest {

    private final PaymentGatewayFactory factory = new PaymentGatewayFactory(
            List.of(new RazorpayStrategy(), new StripeStrategy(), new PaypalStrategy()));

    @Test
    void resolvesEachGateway() {
        assertThat(factory.resolve(PaymentGateway.RAZORPAY).gateway()).isEqualTo(PaymentGateway.RAZORPAY);
        assertThat(factory.resolve(PaymentGateway.STRIPE).gateway()).isEqualTo(PaymentGateway.STRIPE);
        assertThat(factory.resolve(PaymentGateway.PAYPAL).gateway()).isEqualTo(PaymentGateway.PAYPAL);
    }

    @Test
    void refundReturnsReference() {
        PaymentResult result = factory.resolve(PaymentGateway.STRIPE).refund("stripe_abc");
        assertThat(result.success()).isTrue();
        assertThat(result.transactionRef()).startsWith("stripe_refund_");
    }
}
