package com.hotelreservation.payment;

public record PaymentResult(boolean success, String transactionRef, String message) {
    public static PaymentResult ok(String ref) { return new PaymentResult(true, ref, "OK"); }
    public static PaymentResult fail(String msg) { return new PaymentResult(false, null, msg); }
}
