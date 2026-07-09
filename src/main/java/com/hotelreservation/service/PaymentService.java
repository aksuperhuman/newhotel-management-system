package com.hotelreservation.service;

import com.hotelreservation.config.KafkaTopicsConfig;
import com.hotelreservation.domain.Payment;
import com.hotelreservation.domain.enums.PaymentGateway;
import com.hotelreservation.domain.enums.PaymentStatus;
import com.hotelreservation.event.EventPublisher;
import com.hotelreservation.event.PaymentEvent;
import com.hotelreservation.payment.PaymentGatewayFactory;
import com.hotelreservation.payment.PaymentResult;
import com.hotelreservation.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public Payment charge(Long reservationId, BigDecimal amount, PaymentGateway gateway) {
        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .gateway(gateway)
                .amount(amount)
                .status(PaymentStatus.INITIATED)
                .attempts(0)
                .build();

        PaymentResult result = attemptCharge(gateway, amount, "res-" + reservationId, payment);
        payment.setAttempts(payment.getAttempts() + 1);

        if (result.success()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionRef(result.transactionRef());
            paymentRepository.save(payment);
            eventPublisher.publish(KafkaTopicsConfig.PAYMENT_COMPLETED, reservationId.toString(),
                    new PaymentEvent(payment.getId(), reservationId, gateway, amount, PaymentStatus.SUCCESS, java.time.Instant.now()));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            eventPublisher.publish(KafkaTopicsConfig.PAYMENT_FAILED, reservationId.toString(),
                    new PaymentEvent(payment.getId(), reservationId, gateway, amount, PaymentStatus.FAILED, java.time.Instant.now()));
        }
        return payment;
    }

    /**
     * Retry transient gateway failures with exponential backoff. Idempotent from
     * the caller's perspective because we key off the reservation reference.
     */
    @Retryable(retryFor = TransientPaymentException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2))
    protected PaymentResult attemptCharge(PaymentGateway gateway, BigDecimal amount, String ref, Payment payment) {
        PaymentResult result = gatewayFactory.resolve(gateway).charge(amount, ref);
        if (!result.success()) {
            log.warn("Payment attempt failed for {}: {}", ref, result.message());
        }
        return result;
    }

    @Transactional
    public Payment refund(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalStateException("No payment for reservation " + reservationId));
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            gatewayFactory.resolve(payment.getGateway()).refund(payment.getTransactionRef());
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }
        return payment;
    }

    static class TransientPaymentException extends RuntimeException {
        TransientPaymentException(String m) { super(m); }
    }
}
