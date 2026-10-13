# Rockhill Hall Booking - Function 3: Staff & Vendor Assignment
Author: Perera L.D.U.J (IT25300121) - SE2012 Group 26
Spring Boot 3 (Java 17) + Spring Data JPA + MySQL. Layered: controller -> service -> repository -> entity.

## 1. Setup (in this order)
1. In MySQL Workbench run the group script `OOAD_Rockhill_Database.sql` (creates DB `rockhill` + sample data).
2. Then run `sql/StaffAvailability.sql` (adds the StaffAvailability table + a unique key), then `sql/HallDamageReport.sql` (adds the HallDamageReport table).
3. Put your MySQL password in `src/main/resources/application.properties`.
4. Run `HallBookingApplication` (or `mvn spring-boot:run`). Hibernate does NOT touch the tables (`ddl-auto=none`).
5. Test with Postman at `http://localhost:8080`.

## 2. Endpoints (try these with the group sample data)

| Use case | Method & URL | Body |
|---|---|---|
| 1. Create staff | `POST /api/staff` | `{"name":"Amal","role":"Waiter","type":"Internal","contact":"0771100099"}` |
| Update / delete / list | `PUT /api/staff/{id}`, `DELETE /api/staff/{id}`, `GET /api/staff`, `GET /api/staff/{id}` | |
| 2. Mark unavailable | `POST /api/staff/4/unavailable-dates` | `{"date":"2026-12-25","reason":"Leave"}` |
| Reverse the mark | `DELETE /api/staff/4/unavailable-dates/2026-12-25` | |
| List marks | `GET /api/staff/4/unavailable-dates` | |
| 3. Available staff | `GET /api/bookings/5/assignments/available-staff?role=Waiter` | |
| Assign | `POST /api/bookings/5/assignments` | `{"staffIds":[2,4]}` |
| Reassign (replace staff 4) | `PUT /api/bookings/5/assignments/4` | `{"newStaffId":8}` -> 409, 8 is already on booking 5 |
| List assignments | `GET /api/bookings/5/assignments` | |

`type` is `Internal` or `Vendor`; dates are `yyyy-MM-dd`.
Booking 5 (Confirmed, 2026-11-14) is a good test booking. Bookings 7, 8, 9 are Pending -> assigning gives 400.
Marking staff 1 unavailable on 2026-10-24 gives 409 (confirmed assignment on booking 4).

## 3. Matches the report
- Use case 1: name/role/type validation (alt. 3a -> 400); delete blocked if linked to an assignment (alt. 4a -> 409).
- Use case 2: warns (409) if a CONFIRMED assignment exists that date (alt. 3a); mark can be reversed (alt. 5a).
- Use case 3: only Confirmed bookings; lists staff free on the event date; assignment blocks that date for the person; reassign frees the old person; assigned staff "notified" (logged by `LoggingNotificationService`).

## 4. Merging with teammates
- `entity/Booking.java` is a read-only stub: delete it and use Kawmadee's real Booking entity
  (same table `Booking`; needs bookingId, eventDate, status). Delete `BookingRepository` if she has one.
- Cancel Booking (Function 1) should call `StaffAssignmentService.releaseForBooking(bookingId)`
  (the SQL already deletes assignments if a booking row is deleted).
- Keep ONE pom.xml / application.properties / main class for the whole group.

## 5. Front end
Plain HTML/CSS/JS in `src/main/resources/static/` (served by Spring Boot, no npm needed, so no CORS setup either).
Run the app, then open **http://localhost:8080**. Tabs: Staff/Vendors (CRUD), Availability, Assign to Booking.
Added `GET /api/bookings` (BookingController) so the Assign tab can show a booking dropdown.

## 6. Hall damage check (staff admin -> Payment function)
After an event (Booking status = **Completed**) the staff admin opens the **Hall Damage** tab and presses **No damage** or **Hall damaged** (notes required when damaged). One report per booking, stored in table `HallDamageReport`.

| Method & URL | Body |
|---|---|
| `POST /api/bookings/{id}/damage-report` | `{"damaged":true,"notes":"Broken chairs"}` (201; 409 if already exists; 400 if booking not Completed) |
| `PUT /api/bookings/{id}/damage-report` | same body, changes the existing report |
| `GET /api/bookings/{id}/damage-report` | 404 = staff has not reported yet |
| `GET /api/damage-reports` | all reports |

**For the Payment function:** inject `DamageReportService` and call `findByBookingId(bookingId)` -> `Optional<DamageReportResponse>` (`damaged`, `notes`). Empty = not checked yet. Use it to decide whether the extra Rs. 20,000 is returned. Test: `UPDATE Booking SET status='Completed' WHERE booking_id=4;`
