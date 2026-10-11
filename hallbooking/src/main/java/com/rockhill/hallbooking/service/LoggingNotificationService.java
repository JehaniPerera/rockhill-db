package com.rockhill.hallbooking.service;

import com.rockhill.hallbooking.entity.Booking;
import com.rockhill.hallbooking.entity.Staff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LoggingNotificationService implements NotificationService {

    @Override
    public void notifyAssigned(Staff staff, Booking booking) {
        log.info("NOTIFY {} ({}): you are assigned to booking #{} on {}",
                staff.getName(), staff.getContact(), booking.getBookingId(), booking.getEventDate());
    }

    @Override
    public void notifyRemoved(Staff staff, Booking booking) {
        log.info("NOTIFY {} ({}): you are no longer assigned to booking #{} on {}",
                staff.getName(), staff.getContact(), booking.getBookingId(), booking.getEventDate());
    }
}
