package com.rockhill.reception_hall.menu;

import com.rockhill.reception_hall.booking.model.Booking;
import com.rockhill.reception_hall.booking.model.BookingStatus;
import com.rockhill.reception_hall.booking.model.WeddingBooking;
import com.rockhill.reception_hall.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional
public class BookingSelectionService {

    static final int GUESTS_PER_TABLE = 9;

    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookingAddOnRepository bookingAddOnRepository;
    @Autowired private BookingDecorSelectionRepository bookingDecorSelectionRepository;
    @Autowired private AddOnRepository addOnRepository;
    @Autowired private DecorOptionRepository decorOptionRepository;

    public SelectionLine addAddOn(Long bookingId, Integer addonId, Integer quantity) {
        Booking booking = editableBooking(bookingId);
        AddOn addOn = addOnRepository.findById(addonId)
                .orElseThrow(() -> notFound("Add-on not found: " + addonId));
        boolean wedding = booking instanceof WeddingBooking;

        if (!wedding && Boolean.TRUE.equals(addOn.getIsWeddingExclusive())) {
            throw bad(addOn.getName() + " is only available for wedding bookings.");
        }

        int qty = quantity != null ? quantity
                : ("per guest".equals(addOn.getUnitType()) ? booking.getGuestCount() : 1);
        if (qty < 1) {
            throw bad("Quantity must be at least 1 (to remove an add-on, use the remove endpoint).");
        }

        boolean free = wedding && Boolean.TRUE.equals(addOn.getIncludedInPackage());

        BookingAddOn link = bookingAddOnRepository
                .findById(new BookingAddOnId(booking.getId(), addonId))
                .orElseGet(BookingAddOn::new);
        link.setBooking(booking);
        link.setAddOn(addOn);
        link.setQuantity(qty);
        link.setChargedAmount(free ? 0.0 : addOn.getPrice());
        bookingAddOnRepository.save(link);
        booking.touch();   // keeps Function 1's 24-hour draft-expiry clock alive
        return line(link);
    }

    public void removeAddOn(Long bookingId, Integer addonId) {
        Booking booking = editableBooking(bookingId);
        BookingAddOnId id = new BookingAddOnId(booking.getId(), addonId);
        if (!bookingAddOnRepository.existsById(id)) {
            throw notFound("Add-on " + addonId + " is not selected for this booking.");
        }
        bookingAddOnRepository.deleteById(id);
        booking.touch();
    }

    @Transactional(readOnly = true)
    public List<SelectionLine> listAddOns(Long bookingId) {
        return bookingAddOnRepository.findByBooking_Id(getBooking(bookingId).getId())
                .stream().map(BookingSelectionService::line).toList();
    }

    public SelectionLine selectDecor(Long bookingId, Integer decorId) {
        Booking booking = editableBooking(bookingId);
        DecorOption decor = decorOptionRepository.findById(decorId)
                .orElseThrow(() -> notFound("Decor option not found: " + decorId));

        boolean singular = Boolean.TRUE.equals(decor.getIsSingular());
        int qty;
        if (singular) {
            qty = 1;
            for (BookingDecorSelection existing :
                    bookingDecorSelectionRepository.findByBooking_Id(booking.getId())) {
                DecorOption other = existing.getDecorOption();
                if (!other.getDecorId().equals(decorId)
                        && other.getCategory().equals(decor.getCategory())) {
                    bookingDecorSelectionRepository.delete(existing);
                }
            }
        } else {
            qty = (int) Math.ceil(booking.getGuestCount() / (double) GUESTS_PER_TABLE);
        }

        BookingDecorSelection link = bookingDecorSelectionRepository
                .findById(new BookingDecorSelectionId(booking.getId(), decorId))
                .orElseGet(BookingDecorSelection::new);
        link.setBooking(booking);
        link.setDecorOption(decor);
        link.setQuantity(qty);
        link.setChargedAmount(booking instanceof WeddingBooking ? 0.0 : decor.getPrice());
        bookingDecorSelectionRepository.save(link);
        booking.touch();
        return line(link);
    }

    public void removeDecor(Long bookingId, Integer decorId) {
        Booking booking = editableBooking(bookingId);
        BookingDecorSelectionId id = new BookingDecorSelectionId(booking.getId(), decorId);
        if (!bookingDecorSelectionRepository.existsById(id)) {
            throw notFound("Decor option " + decorId + " is not selected for this booking.");
        }
        bookingDecorSelectionRepository.deleteById(id);
        booking.touch();
    }

    @Transactional(readOnly = true)
    public List<SelectionLine> listDecor(Long bookingId) {
        return bookingDecorSelectionRepository.findByBooking_Id(getBooking(bookingId).getId())
                .stream().map(BookingSelectionService::line).toList();
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> notFound("Booking not found"));
    }

    private Booking editableBooking(Long bookingId) {
        Booking b = getBooking(bookingId);
        if (b.getStatus() != BookingStatus.PENDING || b.isSubmitted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking can no longer be edited");
        }
        return b;
    }

    private static SelectionLine line(BookingAddOn l) {
        return new SelectionLine(l.getAddOn().getAddonId(), l.getAddOn().getName(), l.getQuantity(),
                l.getChargedAmount(), l.getChargedAmount() * l.getQuantity());
    }

    private static SelectionLine line(BookingDecorSelection l) {
        return new SelectionLine(l.getDecorOption().getDecorId(), l.getDecorOption().getName(), l.getQuantity(),
                l.getChargedAmount(), l.getChargedAmount() * l.getQuantity());
    }

    private static ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private static ResponseStatusException notFound(String msg) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
    }
}
