package com.rockhill.hallbooking.service;

import com.rockhill.hallbooking.dto.UnavailabilityRequest;
import com.rockhill.hallbooking.dto.UnavailabilityResponse;
import com.rockhill.hallbooking.entity.BookingStatus;
import com.rockhill.hallbooking.entity.Staff;
import com.rockhill.hallbooking.entity.StaffAvailability;
import com.rockhill.hallbooking.exception.ConflictException;
import com.rockhill.hallbooking.exception.ResourceNotFoundException;
import com.rockhill.hallbooking.repository.StaffAssignmentRepository;
import com.rockhill.hallbooking.repository.StaffAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Use case: Mark Staff/Vendor Availability. */
@Service
@RequiredArgsConstructor
@Transactional
public class StaffAvailabilityService {

    private final StaffService staffService;
    private final StaffAvailabilityRepository availabilityRepository;
    private final StaffAssignmentRepository assignmentRepository;

    /** Main flow steps 3-5 + alternate flow 3a. */
    public UnavailabilityResponse markUnavailable(Long staffId, UnavailabilityRequest request) {
        Staff staff = staffService.findStaff(staffId);
        LocalDate date = request.date();

        // 3a: already has a confirmed assignment on that date -> warn, reassign first
        if (assignmentRepository.existsByStaffStaffIdAndAssignedDateAndBookingStatus(
                staffId, date, BookingStatus.CONFIRMED)) {
            throw new ConflictException(staff.getName() + " already has a confirmed assignment on " + date
                    + ". Please reassign the affected booking first.");
        }
        if (availabilityRepository.existsByStaffStaffIdAndUnavailableDate(staffId, date)) {
            throw new ConflictException(staff.getName() + " is already marked unavailable on " + date);
        }

        StaffAvailability availability = new StaffAvailability();
        availability.setStaff(staff);
        availability.setUnavailableDate(date);
        availability.setReason(request.reason());
        return UnavailabilityResponse.from(availabilityRepository.save(availability));
    }

    /** Alternate flow 5a: reverse an unavailable mark. */
    public void markAvailableAgain(Long staffId, LocalDate date) {
        staffService.findStaff(staffId);
        StaffAvailability availability = availabilityRepository
                .findByStaffStaffIdAndUnavailableDate(staffId, date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No unavailable mark found for staff/vendor " + staffId + " on " + date));
        availabilityRepository.delete(availability);
    }

    @Transactional(readOnly = true)
    public List<UnavailabilityResponse> list(Long staffId) {
        staffService.findStaff(staffId);
        return availabilityRepository.findByStaffStaffIdOrderByUnavailableDate(staffId)
                .stream().map(UnavailabilityResponse::from).toList();
    }
}
