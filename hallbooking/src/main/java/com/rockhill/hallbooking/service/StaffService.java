package com.rockhill.hallbooking.service;

import com.rockhill.hallbooking.dto.StaffRequest;
import com.rockhill.hallbooking.dto.StaffResponse;
import com.rockhill.hallbooking.entity.BookingStatus;
import com.rockhill.hallbooking.entity.Staff;
import com.rockhill.hallbooking.exception.ConflictException;
import com.rockhill.hallbooking.exception.ResourceNotFoundException;
import com.rockhill.hallbooking.repository.StaffAssignmentRepository;
import com.rockhill.hallbooking.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Use case: Manage Staff/Vendor Records (create / update / delete). */
@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final StaffAssignmentRepository assignmentRepository;

    public StaffResponse create(StaffRequest request) {
        Staff staff = new Staff();
        apply(staff, request);
        return StaffResponse.from(staffRepository.save(staff));
    }

    public StaffResponse update(Long id, StaffRequest request) {
        Staff staff = findStaff(id);
        apply(staff, request);
        return StaffResponse.from(staffRepository.save(staff));
    }

    /** Alternate flow 4a: block deletion when the staff/vendor is linked to an assignment. */
    public void delete(Long id) {
        Staff staff = findStaff(id);

        if (assignmentRepository.existsByStaffStaffIdAndBookingStatusIn(
                id, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED))) {
            throw new ConflictException("Cannot delete: " + staff.getName()
                    + " is linked to an active booking assignment. Reassign the booking first.");
        }
        if (assignmentRepository.existsByStaffStaffId(id)) {
            throw new ConflictException("Cannot delete: " + staff.getName()
                    + " has past assignment records that must be kept for data integrity.");
        }
        staffRepository.delete(staff);   // blocked dates are removed by cascade
    }

    @Transactional(readOnly = true)
    public StaffResponse get(Long id) {
        return StaffResponse.from(findStaff(id));
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> list() {
        return staffRepository.findAll().stream().map(StaffResponse::from).toList();
    }

    public Staff findStaff(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff/vendor not found with id " + id));
    }

    private void apply(Staff staff, StaffRequest r) {
        staff.setName(r.name().trim());
        staff.setRole(r.role() == null ? null : r.role().trim());
        staff.setType(r.type());
        staff.setContact(r.contact() == null ? null : r.contact().trim());
    }
}
