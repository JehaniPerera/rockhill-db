package com.rockhill.hallbooking.service;

import com.rockhill.hallbooking.dto.DamageReportRequest;
import com.rockhill.hallbooking.dto.DamageReportResponse;
import com.rockhill.hallbooking.entity.Booking;
import com.rockhill.hallbooking.entity.BookingStatus;
import com.rockhill.hallbooking.entity.HallDamageReport;
import com.rockhill.hallbooking.exception.BadRequestException;
import com.rockhill.hallbooking.exception.ConflictException;
import com.rockhill.hallbooking.exception.ResourceNotFoundException;
import com.rockhill.hallbooking.repository.BookingRepository;
import com.rockhill.hallbooking.repository.HallDamageReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Staff admin records whether the hall was damaged after a Completed booking.
 * The Payment function reads the result (see {@link #findByBookingId}) to decide
 * whether the extra 20,000 deposit is returned or kept.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DamageReportService {

    private final BookingRepository bookingRepository;
    private final HallDamageReportRepository reportRepository;

    @Transactional(readOnly = true)
    public DamageReportResponse get(Long bookingId) {
        findBooking(bookingId);
        return reportRepository.findByBookingId(bookingId)
                .map(DamageReportResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No damage report has been submitted for booking " + bookingId));
    }

    @Transactional(readOnly = true)
    public List<DamageReportResponse> listAll() {
        return reportRepository.findAll().stream().map(DamageReportResponse::from).toList();
    }

    /** For the Payment function (Java call, no HTTP needed). Empty = staff has not reported yet. */
    @Transactional(readOnly = true)
    public Optional<DamageReportResponse> findByBookingId(Long bookingId) {
        return reportRepository.findByBookingId(bookingId).map(DamageReportResponse::from);
    }

    public DamageReportResponse create(Long bookingId, DamageReportRequest request) {
        Booking booking = findBooking(bookingId);
        requireCompleted(booking);
        validate(request);
        if (reportRepository.existsByBookingId(bookingId)) {
            throw new ConflictException("A damage report already exists for booking " + bookingId
                    + " (use update to change it)");
        }
        HallDamageReport report = new HallDamageReport();
        report.setBookingId(bookingId);
        apply(report, request);
        try {
            return DamageReportResponse.from(reportRepository.saveAndFlush(report));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("A damage report was just submitted for booking " + bookingId);
        }
    }

    public DamageReportResponse update(Long bookingId, DamageReportRequest request) {
        Booking booking = findBooking(bookingId);
        requireCompleted(booking);
        validate(request);
        HallDamageReport report = reportRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No damage report has been submitted for booking " + bookingId));
        apply(report, request);
        return DamageReportResponse.from(reportRepository.saveAndFlush(report));
    }

    // ---------------- helpers ----------------

    private void apply(HallDamageReport report, DamageReportRequest request) {
        report.setDamaged(request.damaged());
        String notes = request.notes() == null ? "" : request.notes().trim();
        report.setNotes(notes.isEmpty() ? null : notes);
        report.setReportedAt(LocalDateTime.now());
    }

    private void validate(DamageReportRequest request) {
        if (request.damaged() && (request.notes() == null || request.notes().isBlank())) {
            throw new BadRequestException("Please describe the damage when the hall is marked as damaged");
        }
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + id));
    }

    /** The hall can only be checked after the event, i.e. the booking is Completed. */
    private void requireCompleted(Booking booking) {
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Damage can only be reported for a Completed booking (current status: "
                    + booking.getStatus().getDbValue() + ")");
        }
    }
}
