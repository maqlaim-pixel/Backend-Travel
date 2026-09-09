package com.travelvista.service;

/**
 * Thrown when an OTP email cannot be delivered (SMTP failure, auth failure, etc.).
 *
 * Extends RuntimeException so existing controllers that catch RuntimeException
 * (EnquiryController / LeadController OTP endpoints) keep working unchanged.
 * The underlying cause is ALWAYS logged server-side (never returned to the
 * client with credentials/stack trace).
 */
public class OtpDeliveryException extends RuntimeException {

    public OtpDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public OtpDeliveryException(String message) {
        super(message);
    }
}
