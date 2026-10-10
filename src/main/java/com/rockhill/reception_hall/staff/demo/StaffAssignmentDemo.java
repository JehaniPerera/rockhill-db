package com.rockhill.reception_hall.staff.demo;

import com.rockhill.reception_hall.staff.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffAssignmentDemo {
    public static void main(String[] args) {
        List<Staff> staff = new ArrayList<>();
        staff.add(new Staff(1, "Nimal Senanayake", "Event Coordinator", "Internal", "0771100001"));
        staff.add(new Staff(2, "Kasun Bandara", "Head Waiter", "Internal", "0771100002"));
        staff.add(new Staff(3, "Chamari Jayasinghe", "Waitress", "Internal", "0771100003"));
        staff.add(new Staff(4, "Ruwan Samarasinghe", "Head Cook", "Internal", "0771100004"));
        staff.add(new Staff(5, "Sampath Rathnayake", "Security", "Internal", "0771100005"));
        staff.add(new Staff(6, "Dilan Wijesekara", "DJ", "Vendor", "0771100006"));
        staff.add(new Staff(7, "Udara Flora & Decor", "Decorator", "Vendor", "0771100007"));
        staff.add(new Staff(8, "Malith Gunasekara", "Waiter", "Internal", "0771100008"));
        staff.add(new Staff(9, "Helakala Mandapaya", "Dancing Group", "Vendor", "0772200001"));
        staff.add(new Staff(10, "Helakala Mandapaya", "Jaya Mangala Gatha", "Vendor", "0772200002"));

        StaffAssignmentService service = new StaffAssignmentService(staff);
        LocalDate date = LocalDate.of(2027, 2, 14);
        int bookingId = 1;

        System.out.println("Total staff loaded: " + service.getAllStaff().size());

        // Step 1: Client selects a group of staff/vendors they want for the event
        List<Integer> clientPicks = List.of(1, 2, 6, 10); // coordinator, waiter, DJ, Jaya Mangala Gatha
        List<Staff> alreadyBusy = service.clientSelectStaff(bookingId, date, clientPicks);
        System.out.println("Not available at selection time: " + alreadyBusy);
        System.out.println("Pending confirmation: " + service.getPendingConfirmations(bookingId, date).size());

        // Step 2: After the client confirms the booking, the Manager calls everyone to confirm
        service.confirmAvailability(bookingId, date, 1, true);   // coordinator available
        service.confirmAvailability(bookingId, date, 2, true);   // waiter available
        service.confirmAvailability(bookingId, date, 6, true);   // DJ available
        service.confirmAvailability(bookingId, date, 10, false); // Jaya Mangala Gatha NOT available

        System.out.println("Still pending: " + service.getPendingConfirmations(bookingId, date).size());

        // Step 3: Manager finds a replacement for the unavailable one and confirms the new one
        boolean swapped = service.reassign(bookingId, date, 10, 9); // swap with the other Helakala Mandapaya team
        service.confirmAvailability(bookingId, date, 9, true);
        System.out.println("Swapped unavailable staff: " + swapped);

        System.out.println("Final assignments for booking " + bookingId + ": "
                + service.getAssignmentsForBooking(bookingId, date).size());

        // Reporting-style queries (useful for the DBMS report / viva)
        System.out.println("\n--- Report Queries ---");
        System.out.println("Available on " + date + ": " + service.getAvailableStaff(date, null));
        System.out.println("Confirmed staff for booking: " + service.getConfirmedStaffForBooking(bookingId, date));
        System.out.println("Unavailable on date: " + service.getUnavailableStaff(date));
        System.out.println("Assigned to multiple events: " + service.getStaffAssignedToMultipleEvents());
    }
}
