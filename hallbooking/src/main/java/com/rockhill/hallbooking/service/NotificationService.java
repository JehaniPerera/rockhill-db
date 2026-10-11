package com.rockhill.hallbooking.service;

import com.rockhill.hallbooking.entity.Booking;
import com.rockhill.hallbooking.entity.Staff;

/** Secondary actor "Staff/Vendor (notified)" in the use case diagram. */
public interface NotificationService {

    void notifyAssigned(Staff staff, Booking booking);

    void notifyRemoved(Staff staff, Booking booking);
}
