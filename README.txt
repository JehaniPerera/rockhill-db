FUNCTION 3 -> demo5 MERGE  (no change to ANY demo5 file)
=========================================================
Work on your Function 3 project, then merge it into demo5 as usual.

1) Copy the folder "src" from this zip over the "src" folder of your Function 3 project (overwrite all).
   Changed:  staff/dto/BookingResponse.java, staff/exception/GlobalExceptionHandler.java,
             staff/repository/StaffAssignmentRepository.java, staff/controller/DamageReportController.java,
             staff/service/DamageReportService.java, LoggingNotificationService.java, NotificationService.java,
             StaffAssignmentService.java, StaffAvailabilityService.java, StaffService.java,
             static/js/staff-app.js
   New:      staff/controller/StaffBookingController.java   (replaces BookingController)
             staff/service/StaffAssignmentCompatConfig.java (makes demo5's "StaffAssignment" SQL work)
             static/admin/staff/index.html                  (Function 3's index.html, moved + paths fixed)
   Same:     static/css/staff-theme.css

2) In your Function 3 project DELETE:
   staff/entity/Booking.java
   staff/entity/BookingStatus.java
   staff/entity/BookingStatusConverter.java
   staff/repository/BookingRepository.java
   staff/controller/BookingController.java
   static/index.html            (it would replace demo5's customer home page)

3) When merging into demo5 take ONLY the "staff" package and the files listed above under static/.
   Do NOT take from Function 3: ReceptionHallApplication.java, application.properties, pom.xml,
   00_setup.sql, tests.  demo5's own files stay exactly as they are.
