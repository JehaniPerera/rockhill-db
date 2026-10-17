package com.rockhill.reception_hall.portal;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * One place for every access rule.
 *
 * ADMIN    -> admin pages and admin APIs only.
 * CUSTOMER -> customer pages and customer APIs only.
 * Nobody signed in -> only the login and register pages.
 *
 * NOTE: this class is deliberately NOT called SecurityConfig. Function 4 has its own
 * SecurityConfig.java (permit-all). That file must be deleted, because two
 * "match every request" filter chains stop the app from starting.
 */
@Configuration
@EnableWebSecurity
public class PortalSecurityConfig {

    private static final String[] CATALOG_API = {
            "/api/packages/**", "/api/menus/**", "/api/addons/**",
            "/api/decor-options/**", "/api/package-pricing-tiers/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** After login: admin goes to the admin page, customer goes to the customer home page. */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(admin ? "/admin/index.html" : "/index.html");
        };
    }

    @Bean
    public SecurityFilterChain portalFilterChain(HttpSecurity http,
                                                 PortalUserRepository users,
                                                 AuthenticationSuccessHandler successHandler) throws Exception {
        RequestMatcher api = request -> request.getRequestURI().startsWith("/api/");

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // ---- public: login, register, shared assets ----
                .requestMatchers("/login.html", "/register.html", "/forbidden.html",
                        "/css/portal.css", "/js/portal.js",
                        "/api/auth/register", "/api/auth/status", "/error").permitAll()
                .requestMatchers("/booking/css/**", "/booking/js/**", "/css/menu-decor.css",
                        "/css/style.css", "/js/menu-decor-nav.js", "/js/menu-decor-selection.js").permitAll()

                // ---- admin only: pages ----
                .requestMatchers("/admin/**", "/admin.html", "/booking/booking-admin.html",
                        "/css/admin.html").hasRole("ADMIN")
                // ---- admin only: APIs ----
                .requestMatchers("/api/staff/**", "/api/bookings/*/assignments/**",
                        "/api/balance-reminders/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/bookings/deposits/sync",
                        "/api/bookings/*/confirm", "/api/bookings/*/reject",
                        "/api/bookings/*/deposit", "/api/bookings/*/complete").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, CATALOG_API).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, CATALOG_API).hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, CATALOG_API).hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/payments/refund/*", "/api/payments/invoice/*",
                        "/api/payment-proofs/approve/*", "/api/payment-proofs/reject/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/payments", "/api/payment-proofs/pending").hasRole("ADMIN")

                // ---- customer only: pages ----
                .requestMatchers("/index.html", "/customer.html", "/booking/**",
                        "/menus", "/menus/**", "/packages", "/packages/**").hasRole("CUSTOMER")
                // ---- customer only: APIs that create or change a customer's own booking ----
                .requestMatchers(HttpMethod.POST, "/api/bookings/wedding", "/api/bookings/event",
                        "/api/bookings/*/submit").hasRole("CUSTOMER")
                .requestMatchers("/api/bookings/*/selections/**").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/payment-proofs/submit/*").hasRole("CUSTOMER")

                // ---- everything else (home redirect, /api/auth/me, shared catalog reads,
                //      invoice.html, receipt.html ...) just needs a login ----
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login.html?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html?logout")
                .permitAll())
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), api)
                .accessDeniedHandler((request, response, denied) -> {
                    if (api.matches(request)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Your account type cannot do this.\"}");
                    } else {
                        response.sendRedirect("/forbidden.html");
                    }
                }))
            .addFilterAfter(new CustomerOwnershipFilter(users), AuthorizationFilter.class);

        return http.build();
    }
}
