package com.hotel.reservation.service;

import com.hotel.reservation.config.KafkaTopicConfig;
import com.hotel.reservation.domain.*;
import com.hotel.reservation.event.PaymentEvent;
import com.hotel.reservation.kafka.EventPublisher;
import com.hotel.reservation.payment.PaymentGateway;
import com.hotel.reservation.payment.PaymentGatewayFactory;
import com.hotel.reservation.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    /**
     * Charge with a bounded retry. Transient gateway failures are retried up
     * to 3 times with exponential backoff; a definitive decline just returns
     * a FAILED payment.
     */
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    @Transactional
    public Payment charge(Reservation reservation, PaymentProvider provider) {
        PaymentGateway gateway = gatewayFactory.resolve(provider);
        var result = gateway.charge(reservation.getReference(), reservation.getTotalAmount());

        Payment payment = Payment.builder()
            .reservationId(reservation.getId())
            .provider(provider)
            .providerRef(result.providerRef())
            .amount(reservation.getTotalAmount())
            .status(result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
            .build();
        payment = paymentRepository.save(payment);

        String topic = result.success() ? KafkaTopicConfig.PAYMENT_COMPLETED : KafkaTopicConfig.PAYMENT_FAILED;
        eventPublisher.publish(topic, reservation.getReference(),
            PaymentEvent.of(result.success() ? "PaymentCompleted" : "PaymentFailed",
                payment.getId(), reservation.getId(), provider.name(),
                payment.getAmount(), result.success()));
        return payment;
    }

    @Transactional
    public Payment refund(Payment payment) {
        PaymentGateway gateway = gatewayFactory.resolve(payment.getProvider());
        gateway.refund(payment.getProviderRef(), payment.getAmount());
        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public BigDecimal reservationTotal(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).stream()
            .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
