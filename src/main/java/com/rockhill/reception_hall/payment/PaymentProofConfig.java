package com.rockhill.reception_hall.payment;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentProofConfig {

    // The PaymentProof table is now created by schema.sql (src/main/resources),
    // so the old createPaymentProofTable bean is removed.

    // the server accepts uploads up to 10 MB; PaymentProofService enforces the friendly 5 MB limit
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("", 10L * 1024 * 1024, 12L * 1024 * 1024, 0);
    }
}