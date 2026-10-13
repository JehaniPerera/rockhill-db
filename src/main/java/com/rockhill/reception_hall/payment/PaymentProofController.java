package com.rockhill.reception_hall.payment;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-proofs")
public class PaymentProofController {

    private final PaymentProofService service;

    public PaymentProofController(PaymentProofService service) {
        this.service = service;
    }

    // customer: upload the receipt for a deposit, balance or full payment
    @PostMapping("/submit/{bookingId}")
    public String submit(@PathVariable int bookingId,
                         @RequestParam("type") String type,
                         @RequestParam(value = "reference", required = false) String reference,
                         @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        service.submit(bookingId, type, reference, file);
        return "Receipt uploaded. Your payment is now Pending Approval.";
    }

    // customer and admin: every proof of one booking, newest first
    @GetMapping("/booking/{bookingId}")
    public List<Map<String, Object>> forBooking(@PathVariable int bookingId) {
        return service.forBooking(bookingId);
    }

    // admin: everything waiting for approval
    @GetMapping("/pending")
    public List<Map<String, Object>> pending() {
        return service.pending();
    }

    // admin: open the uploaded receipt
    @GetMapping("/file/{proofId}")
    public ResponseEntity<Resource> file(@PathVariable int proofId) {
        PaymentProofService.ProofFile f = service.getFile(proofId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.contentType()))
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Disposition", "inline")
                .header("Cache-Control", "private, no-store")
                .body(new FileSystemResource(f.path()));
    }

    @PostMapping("/approve/{proofId}")
    public String approve(@PathVariable int proofId) {
        return service.approve(proofId);
    }

    @PostMapping("/reject/{proofId}")
    public String reject(@PathVariable int proofId,
                         @RequestParam(value = "reason", required = false) String reason) {
        return service.reject(proofId, reason);
    }

    // business-rule errors become a 400 with a readable message
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleBusinessRule(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleFileProblem(IOException e) {
        return ResponseEntity.status(500).body("The receipt file could not be saved. Please try again.");
    }
}