package com.rockhill.reception_hall.portal;

import com.rockhill.reception_hall.booking.model.Customer;
import com.rockhill.reception_hall.booking.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Pattern PHONE = Pattern.compile("^(\\d{10}|\\+\\d{9,14})$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final PortalUserRepository users;
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;

    public AuthController(PortalUserRepository users, CustomerRepository customers, PasswordEncoder encoder) {
        this.users = users;
        this.customers = customers;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> register(@RequestBody RegisterRequest r) {
        String name = r.fullName() == null ? "" : r.fullName().trim();
        String phone = r.phone() == null ? "" : r.phone().trim();
        String email = r.email() == null ? "" : r.email().trim().toLowerCase();
        String password = r.password() == null ? "" : r.password();

        if (name.isEmpty()) throw bad("Please enter your name.");
        if (!PHONE.matcher(phone).matches())
            throw bad("Phone number must be 10 digits (like 0771234567) or start with + and the country code.");
        if (!EMAIL.matcher(email).matches()) throw bad("Please enter a valid email address.");
        if (password.length() < 8) throw bad("Password must be at least 8 characters.");
        if (users.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");

        Customer customer = customers.findFirstByEmail(email)
                .orElseGet(() -> customers.findByPhone(phone).stream().findFirst()
                        .orElseGet(() -> customers.save(new Customer(name, phone, email))));

        PortalUser user = users.save(new PortalUser(email, encoder.encode(password), name, phone,
                PortalRole.CUSTOMER, customer.getId()));
        return profile(user);
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        PortalUser user = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
        return profile(user);
    }

    /** Used by Function 4's admin.html to decide whether to show its Log out button. */
    @GetMapping("/status")
    public Map<String, Object> status(Authentication authentication) {
        boolean in = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("loggedIn", in);
        if (in) {
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            m.put("role", admin ? "ADMIN" : "CUSTOMER");
        }
        return m;
    }

    /** Used by Function 4's admin.html Log out button. */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return Map.of("loggedIn", false);
    }

    private static Map<String, Object> profile(PortalUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("email", u.getEmail());
        m.put("fullName", u.getFullName());
        m.put("phone", u.getPhone());
        m.put("role", u.getRole().name());
        m.put("customerId", u.getCustomerId());
        return m;
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
