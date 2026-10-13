package com.rockhill.reception_hall.staff.service;

import com.rockhill.reception_hall.staff.entity.Booking;
import com.rockhill.reception_hall.staff.entity.Staff;
import org.springframework.stereotype.Service;


@Service
public class LoggingNotificationService implements NotificationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingNotificationService.class);

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
