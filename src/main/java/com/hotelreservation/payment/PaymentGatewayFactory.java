package com.hotelreservation.payment;

import com.hotelreservation.domain.enums.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory pattern: resolves the correct PaymentGatewayStrategy for a gateway.
 * Spring injects every strategy bean; we index them by their declared gateway.
 */
@Component
public class PaymentGatewayFactory {

    private final Map<PaymentGateway, PaymentGatewayStrategy> strategies = new EnumMap<>(PaymentGateway.class);

    public PaymentGatewayFactory(List<PaymentGatewayStrategy> strategyBeans) {
        strategyBeans.forEach(s -> strategies.put(s.gateway(), s));
    }

    public PaymentGatewayStrategy resolve(PaymentGateway gateway) {
        PaymentGatewayStrategy strategy = strategies.get(gateway);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment gateway: " + gateway);
        }
        return strategy;
    }
}
