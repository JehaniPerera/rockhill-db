package com.rockhill.reception_hall.menu;

import com.rockhill.reception_hall.booking.model.OtherEventBooking;
import com.rockhill.reception_hall.booking.model.WeddingBooking;
import com.rockhill.reception_hall.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingUsageChecker implements CatalogUsageChecker {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public boolean isMenuUsed(Integer menuId) {
        return bookingRepository.findAll().stream().anyMatch(b ->
                b instanceof OtherEventBooking o
                        && o.getMenuId() != null
                        && o.getMenuId().intValue() == menuId);
    }

    @Override
    public boolean isPackageUsed(Integer packageId) {
        return bookingRepository.findAll().stream().anyMatch(b ->
                b instanceof WeddingBooking w
                        && w.getPackageId() != null
                        && w.getPackageId().intValue() == packageId);
    }
}
