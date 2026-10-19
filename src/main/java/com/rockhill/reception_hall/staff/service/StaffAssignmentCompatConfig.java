package com.rockhill.reception_hall.staff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The booking code (Function 1) updates and deletes staff assignments with plain SQL on a table
 * called "StaffAssignment", but Hibernate creates this entity's table as "staff_assignment".
 * This creates a view with the old name so those statements work without changing Function 1.
 * Safe to run on every start.
 */
@Configuration
public class StaffAssignmentCompatConfig {

    private static final Logger log = LoggerFactory.getLogger(StaffAssignmentCompatConfig.class);

    @Bean
    public ApplicationRunner staffAssignmentLegacyName(JdbcTemplate jdbc) {
        return args -> {
            try {
                jdbc.execute("CREATE OR REPLACE VIEW StaffAssignment AS SELECT * FROM staff_assignment");
            } catch (Exception e) {
                log.warn("Could not create the StaffAssignment view (booking cancel/reschedule will not update staff): {}",
                        e.getMessage());
            }
        };
    }
}
