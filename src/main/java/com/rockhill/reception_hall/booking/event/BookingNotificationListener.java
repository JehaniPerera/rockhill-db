package com.rockhill.reception_hall.booking.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BookingNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(BookingNotificationListener.class);

    @EventListener
    public void onBookingEvent(BookingEvent e) {
        log.info("NOTIFY [{}] booking #{} for {}", e.type(), e.booking().getId(),
                e.booking().getCustomer().getName());
    }
}