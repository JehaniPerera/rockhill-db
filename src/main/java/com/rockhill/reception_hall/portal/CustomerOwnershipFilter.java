package com.rockhill.reception_hall.portal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CustomerOwnershipFilter extends OncePerRequestFilter {

    private final PortalUserRepository users;

    public CustomerOwnershipFilter(PortalUserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean customer = auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (customer && request.getRequestURI().equals("/api/bookings/by-phone")) {
            String asked = clean(request.getParameter("phone"));
            String own = users.findByEmailIgnoreCase(auth.getName()).map(u -> clean(u.getPhone())).orElse("");
            if (asked.isEmpty() || !asked.equals(own)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"You can only look up your own bookings (use the phone number you registered with).\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static String clean(String phone) {
        return phone == null ? "" : phone.replaceAll("\\s", "");
    }
}
