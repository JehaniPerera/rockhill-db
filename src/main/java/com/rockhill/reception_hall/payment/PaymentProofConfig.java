package com.rockhill.reception_hall.payment;

import jakarta.servlet.MultipartConfigElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PaymentProofConfig {

    private static final Logger log = LoggerFactory.getLogger(PaymentProofConfig.class);

    // copy this statement into the shared SQL file too, so the database report shows the table
    static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS PaymentProof (
                proof_id INT PRIMARY KEY AUTO_INCREMENT,
                booking_id INT NOT NULL,
                payment_type ENUM('Deposit','Balance','Full') NOT NULL,
                amount DECIMAL(12,2) NOT NULL,
                bank_reference VARCHAR(60) NULL,
                file_name VARCHAR(80) NOT NULL,
                content_type VARCHAR(50) NOT NULL,
                status ENUM('Pending Approval','Approved','Rejected') NOT NULL DEFAULT 'Pending Approval',
                rejection_reason VARCHAR(255) NULL,
                submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                reviewed_at DATETIME NULL,
                CONSTRAINT fk_proof_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
                CONSTRAINT chk_proof_amount CHECK (amount > 0)
            )
            """;

    // creates the PaymentProof table every time the app starts (does nothing if it already exists)
    @Bean
    public ApplicationRunner createPaymentProofTable(JdbcTemplate jdbc) {
        return args -> {
            try {
                jdbc.execute(CREATE_TABLE);
            } catch (Exception e) {
                log.error("Could not create the PaymentProof table: {}", e.getMessage());
            }
        };
    }

    // the server accepts uploads up to 10 MB; PaymentProofService enforces the friendly 5 MB limit
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("", 10L * 1024 * 1024, 12L * 1024 * 1024, 0);
    }
}