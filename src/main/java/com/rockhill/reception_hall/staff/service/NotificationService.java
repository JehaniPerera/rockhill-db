package com.rockhill.reception_hall.staff.service;

import com.rockhill.reception_hall.staff.entity.Booking;
import com.rockhill.reception_hall.staff.entity.Staff;

/** Secondary actor "Staff/Vendor (notified)" in the use case diagram. */
public interface NotificationService {

    void notifyAssigned(Staff staff, Booking booking);

    void notifyRemoved(Staff staff, Booking booking);
}
