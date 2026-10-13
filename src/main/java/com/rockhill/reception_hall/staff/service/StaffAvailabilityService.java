package com.rockhill.reception_hall.staff.service;

import com.rockhill.reception_hall.staff.dto.UnavailabilityRequest;
import com.rockhill.reception_hall.staff.dto.UnavailabilityResponse;
import com.rockhill.reception_hall.staff.entity.BookingStatus;
import com.rockhill.reception_hall.staff.entity.Staff;
import com.rockhill.reception_hall.staff.entity.StaffAvailability;
import com.rockhill.reception_hall.staff.exception.ConflictException;
import com.rockhill.reception_hall.staff.exception.ResourceNotFoundException;
import com.rockhill.reception_hall.staff.repository.StaffAssignmentRepository;
import com.rockhill.reception_hall.staff.repository.StaffAvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Use case: Mark Staff/Vendor Availability. */
@Service
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

    public StaffAvailabilityService(StaffService staffService, StaffAvailabilityRepository availabilityRepository, StaffAssignmentRepository assignmentRepository) {
        this.staffService = staffService;
        this.availabilityRepository = availabilityRepository;
        this.assignmentRepository = assignmentRepository;
    }
}
