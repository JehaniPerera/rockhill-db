package com.rockhill.reception_hall.menu;

import com.rockhill.reception_hall.booking.model.Booking;
import com.rockhill.reception_hall.booking.model.OtherEventBooking;
import com.rockhill.reception_hall.booking.model.WeddingBooking;
import com.rockhill.reception_hall.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PricingService {

    static final double LOW_GUEST_SURCHARGE = 35000.0;
    static final int LOW_GUEST_THRESHOLD = 150;

    @Autowired private PackagePricingTierService tierService;
    @Autowired private MenuRepository menuRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookingAddOnRepository bookingAddOnRepository;
    @Autowired private BookingDecorSelectionRepository bookingDecorSelectionRepository;

    public PriceEstimate estimate(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        return estimate(booking);
    }

    public PriceEstimate estimate(Booking booking) {
        if (booking instanceof WeddingBooking w) {
            return weddingEstimate(w);
        }
        if (booking instanceof OtherEventBooking o) {
            return otherEventEstimate(o);
        }
        throw new IllegalStateException("Unsupported booking type: " + booking.getClass().getSimpleName());
    }

    private PriceEstimate weddingEstimate(WeddingBooking wb) {
        if (wb.getPackageId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a wedding package first");
        }
        int guests = wb.getGuestCount();
        double base = tierService.findTierForGuestCount(wb.getPackageId().intValue(), guests)
                .map(PackagePricingTier::getTotalPrice)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No pricing tier found for " + guests + " guests."));
        double addOns = addOnTotal(wb.getId());
        double decor = decorTotal(wb.getId());   // 0 for weddings by construction
        return new PriceEstimate("Wedding", guests, round(base), 0.0,
                round(addOns), round(decor), round(base + addOns + decor));
    }

    private PriceEstimate otherEventEstimate(OtherEventBooking oeb) {
        if (oeb.getMenuId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a catering menu first");
        }
        int guests = oeb.getGuestCount();
        Menu menu = menuRepository.findById(oeb.getMenuId().intValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Menu not found: " + oeb.getMenuId()));
        double base = menu.getPricePerGuest() * guests;
        double surcharge = guests < LOW_GUEST_THRESHOLD ? LOW_GUEST_SURCHARGE : 0.0;
        double addOns = addOnTotal(oeb.getId());
        double decor = decorTotal(oeb.getId());
        return new PriceEstimate("Other", guests, round(base), surcharge,
                round(addOns), round(decor), round(base + surcharge + addOns + decor));
    }

    private double addOnTotal(Long bookingId) {
        return bookingAddOnRepository.findByBooking_Id(bookingId).stream()
                .mapToDouble(l -> l.getChargedAmount() * l.getQuantity())
                .sum();
    }

    private double decorTotal(Long bookingId) {
        return bookingDecorSelectionRepository.findByBooking_Id(bookingId).stream()
                .mapToDouble(l -> l.getChargedAmount() * l.getQuantity())
                .sum();
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}