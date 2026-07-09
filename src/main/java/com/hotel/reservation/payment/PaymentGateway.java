package com.hotel.reservation.payment;

import com.hotel.reservation.domain.PaymentProvider;

import java.math.BigDecimal;

/**
 * Strategy interface. Each concrete gateway (Razorpay/Stripe/PayPal) provides
 * its own charge/refund behavior; the reservation flow depends only on this
 * abstraction (Dependency Inversion).
 */
public interface PaymentGateway {

    PaymentProvider provider();

    PaymentResult charge(String reference, BigDecimal amount);

    PaymentResult refund(String providerRef, BigDecimal amount);

    record PaymentResult(boolean success, String providerRef, String message) {}
}
