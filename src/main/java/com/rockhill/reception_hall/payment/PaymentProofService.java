package com.rockhill.reception_hall.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentProofService {

    static final long MAX_BYTES = 5L * 1024 * 1024;
    static final int MAX_REFERENCE = 60;
    static final int MAX_REASON = 255;
    static final String PENDING = "Pending Approval";

    public enum ProofType {
        DEPOSIT("Deposit"), BALANCE("Balance"), FULL("Full");

        private final String dbValue;

        ProofType(String dbValue) { this.dbValue = dbValue; }

        public String dbValue() { return dbValue; }

        static ProofType from(String text) {
            if (text != null) {
                for (ProofType t : values()) {
                    if (t.name().equalsIgnoreCase(text.trim())) {
                        return t;
                    }
                }
            }
            throw new IllegalStateException("Unknown payment type. Choose deposit, balance or full.");
        }
    }

    public record ProofFile(Path path, String contentType) {
    }

    private record StoredFile(String fileName, String contentType) {
    }

    private final PaymentService paymentService;
    private final PaymentProofRepository proofs;
    private final Path uploadDir;

    public PaymentProofService(PaymentService paymentService, PaymentProofRepository proofs,
                               @Value("${rockhill.upload.dir:uploads/payment-proofs}") String uploadDir) {
        this.paymentService = paymentService;
        this.proofs = proofs;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    // ---------------------------------------------------------------- customer

    // amountText is only used for balance payments (an installment); the deposit and the full payment have fixed amounts
    public PaymentProof submit(int bookingId, String typeText, String amountText, String reference,
                               MultipartFile file) throws IOException {
        ProofType type = ProofType.from(typeText);
        BigDecimal amount = validateAndPrice(bookingId, type, amountText);   // same rules as a real payment
        if (proofs.countPending(bookingId) > 0) {
            throw new IllegalStateException("A payment proof for this booking is already waiting for approval.");
        }
        String bankReference = cleanReference(reference);
        StoredFile stored = storeFile(file);
        try {
            int id = proofs.insert(bookingId, type.dbValue(), amount, bankReference,
                    stored.fileName(), stored.contentType());
            return proofs.findById(id)
                    .orElseThrow(() -> new IllegalStateException("The payment proof could not be saved."));
        } catch (RuntimeException e) {
            deleteQuietly(stored.fileName());
            throw e;
        }
    }

    public List<Map<String, Object>> forBooking(int bookingId) {
        return proofs.findByBooking(bookingId);
    }

    // ------------------------------------------------------------------- admin

    public List<Map<String, Object>> pending() {
        return proofs.findPending();
    }

    public ProofFile getFile(int proofId) {
        PaymentProof p = proofs.findById(proofId)
                .orElseThrow(() -> new IllegalStateException("Payment proof not found"));
        Path path = uploadDir.resolve(p.fileName()).normalize();
        if (!path.startsWith(uploadDir) || !Files.isRegularFile(path)) {
            throw new IllegalStateException("The receipt file is missing on this computer.");
        }
        return new ProofFile(path, p.contentType());
    }

    // the proof is marked Approved and the real payment is recorded in ONE transaction:
    // if recording fails (for example the booking was cancelled), nothing is saved
    @Transactional
    public String approve(int proofId) {
        PaymentProof p = requirePending(proofId);
        if (proofs.markApproved(proofId) != 1) {
            throw new IllegalStateException("This payment proof was already reviewed.");
        }
        int bookingId = p.bookingId();
        return switch (ProofType.from(p.paymentType())) {
            case DEPOSIT -> {
                paymentService.recordDeposit(bookingId);
                yield "Deposit approved. The booking is now Confirmed.";
            }
            case BALANCE -> {
                paymentService.recordBalance(bookingId, p.amount());
                BigDecimal left = paymentService.balanceRemainingFor(bookingId);
                yield left.signum() == 0
                        ? "Balance approved. The booking is now fully paid."
                        : "Installment approved. " + money(left) + " of the balance is still remaining.";
            }
            case FULL -> {
                paymentService.recordFullPayment(bookingId);
                yield "Full payment approved. The booking is now Confirmed and fully paid.";
            }
        };
    }

    @Transactional
    public String reject(int proofId, String reason) {
        String clean = reason == null ? "" : reason.trim();
        if (clean.isEmpty()) {
            throw new IllegalStateException("Please give a reason, so the customer knows what to correct.");
        }
        if (clean.length() > MAX_REASON) {
            throw new IllegalStateException("The reason is too long (maximum 255 characters).");
        }
        requirePending(proofId);
        if (proofs.markRejected(proofId, clean) != 1) {
            throw new IllegalStateException("This payment proof was already reviewed.");
        }
        return "Payment rejected. The customer can see your reason and upload a new receipt.";
    }

    // ----------------------------------------------------------------- helpers

    private BigDecimal validateAndPrice(int bookingId, ProofType type, String amountText) {
        return switch (type) {
            case DEPOSIT -> {
                paymentService.validateDeposit(bookingId);
                yield paymentService.depositAmount();
            }
            case BALANCE -> paymentService.validateBalance(bookingId, parseAmount(amountText));
            case FULL -> {
                paymentService.validateFull(bookingId);
                yield paymentService.fullAmountFor(bookingId);
            }
        };
    }

    private static BigDecimal parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return null;                    // no amount typed: pay everything that is left
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("The amount must be a number, for example 50000.");
        }
    }

    private static String money(BigDecimal amount) {
        return String.format(Locale.US, "Rs. %,.2f", amount);
    }

    private PaymentProof requirePending(int proofId) {
        PaymentProof p = proofs.findById(proofId)
                .orElseThrow(() -> new IllegalStateException("Payment proof not found"));
        if (!PENDING.equals(p.status())) {
            throw new IllegalStateException("This payment proof was already " + p.status().toLowerCase() + ".");
        }
        return p;
    }

    private String cleanReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }
        String clean = reference.trim();
        if (clean.length() > MAX_REFERENCE) {
            throw new IllegalStateException("The bank reference is too long (maximum 60 characters).");
        }
        return clean;
    }

    private StoredFile storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("Please choose a receipt file to upload.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalStateException("The file is too large. Please upload a file smaller than 5 MB.");
        }
        byte[] data = file.getBytes();
        String[] kind = detectType(data);          // we trust the file contents, not the name the browser sends
        if (kind == null) {
            throw new IllegalStateException("Only JPG, PNG, WEBP or PDF files are accepted.");
        }
        Files.createDirectories(uploadDir);
        String fileName = UUID.randomUUID() + "." + kind[1];   // our own random name, never the customer's
        Files.write(uploadDir.resolve(fileName), data, StandardOpenOption.CREATE_NEW);
        return new StoredFile(fileName, kind[0]);
    }

    // looks at the first bytes of the file: returns {content type, extension} or null when not allowed
    static String[] detectType(byte[] d) {
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return new String[]{"image/jpeg", "jpg"};
        }
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G'
                && d[4] == 0x0D && d[5] == 0x0A && d[6] == 0x1A && d[7] == 0x0A) {
            return new String[]{"image/png", "png"};
        }
        if (d.length >= 5 && d[0] == '%' && d[1] == 'P' && d[2] == 'D' && d[3] == 'F' && d[4] == '-') {
            return new String[]{"application/pdf", "pdf"};
        }
        if (d.length >= 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return new String[]{"image/webp", "webp"};
        }
        return null;
    }

    private void deleteQuietly(String fileName) {
        try {
            Files.deleteIfExists(uploadDir.resolve(fileName));
        } catch (IOException ignored) {
            // nothing more we can do
        }
    }
}