package com.rockhill.reception_hall.booking.dto;

import com.rockhill.reception_hall.booking.model.*;
import java.time.LocalDate;

public record BookingResponse(Long id, String customerName, LocalDate eventDate, EventType eventType,
                              int guestCount, BookingStatus status, boolean submitted, boolean venueDamaged,
                              LocalDate depositDueDate, LocalDate balanceDueDate, String cancellationReason,
                              Long packageId, Long menuId, String eventDescription, String venueDamageNotes) {
    public static BookingResponse from(Booking b) {
        boolean confirmed = b.getStatus() == BookingStatus.CONFIRMED;
        Long pkg = b instanceof WeddingBooking w ? w.getPackageId() : null;
        Long menu = b instanceof OtherEventBooking o ? o.getMenuId() : null;
        String desc = b instanceof OtherEventBooking o2 ? o2.getEventDescription() : null;
        return new BookingResponse(b.getId(), b.getCustomer().getName(), b.getEventDate(), b.getEventType(),
                b.getGuestCount(), b.getStatus(), b.isSubmitted(), b.isVenueDamaged(),
                confirmed ? b.getDepositDueDate() : null,
                confirmed ? b.getBalanceDueDate() : null,
                b.getCancellationReason(), pkg, menu, desc, b.getVenueDamageNotes());
    }
}