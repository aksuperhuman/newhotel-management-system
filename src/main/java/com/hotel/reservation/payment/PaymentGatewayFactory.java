package com.hotel.reservation.payment;

import com.hotel.reservation.domain.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory pattern: resolves the right PaymentGateway strategy at runtime from
 * the requested provider. Spring injects every PaymentGateway bean, so adding
 * a new provider is just adding a new @Component - no change here.
 */
@Component
public class PaymentGatewayFactory {

    private final Map<PaymentProvider, PaymentGateway> gateways = new EnumMap<>(PaymentProvider.class);

    public PaymentGatewayFactory(List<PaymentGateway> gatewayBeans) {
        gatewayBeans.forEach(g -> gateways.put(g.provider(), g));
    }

    public PaymentGateway resolve(PaymentProvider provider) {
        PaymentGateway gateway = gateways.get(provider);
        if (gateway == null) {
            throw new IllegalArgumentException("Unsupported payment provider: " + provider);
        }
        return gateway;
    }
}
