package com.rockhill.reception_hall.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PortalDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PortalDataInitializer.class);

    private final PortalUserRepository users;
    private final PasswordEncoder encoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public PortalDataInitializer(PortalUserRepository users, PasswordEncoder encoder,
                                 @Value("${portal.admin.name:Administrator}") String adminName,
                                 @Value("${portal.admin.email:admin@rockhill.lk}") String adminEmail,
                                 @Value("${portal.admin.password:Admin@123}") String adminPassword) {
        this.users = users;
        this.encoder = encoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (users.existsByRole(PortalRole.ADMIN)) {
            return;
        }
        users.save(new PortalUser(adminEmail.trim().toLowerCase(), encoder.encode(adminPassword),
                adminName, null, PortalRole.ADMIN, null));
        log.info("Created the first admin account: {}", adminEmail);
    }
}
