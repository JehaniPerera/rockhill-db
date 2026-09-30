DROP DATABASE IF EXISTS rockhill;
CREATE DATABASE rockhill;
USE rockhill;

-- CREATING THE TABLES

CREATE TABLE Customer (
customer_id INT PRIMARY KEY AUTO_INCREMENT ,
name VARCHAR(100) NOT NULL,
phone VARCHAR(15) NOT NULL,
email VARCHAR(100) NULL,
CONSTRAINT uq_customer_email UNIQUE(email)
);

CREATE TABLE Booking (
booking_id INT PRIMARY KEY AUTO_INCREMENT,
customer_id INT NOT NULL,
event_date DATE NOT NULL,
event_type ENUM('Wedding','Other') NOT NULL,
guest_count INT NOT NULL,
status ENUM('Pending','Confirmed','Completed','Cancelled') NOT NULL DEFAULT 'Pending',
total_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
venue_damaged BOOLEAN NOT NULL DEFAULT FALSE,
CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
CONSTRAINT chk_booking_guests CHECK (guest_count > 0),
CONSTRAINT chk_booking_total CHECK (total_price >= 0)
);

CREATE TABLE Package (
package_id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(100) NOT NULL,
description VARCHAR(500) NULL,
CONSTRAINT uq_package_name UNIQUE (name)
);


CREATE TABLE PackagePricingTier (
tier_id INT PRIMARY KEY AUTO_INCREMENT,
package_id INT NOT NULL,
tier_number INT NOT NULL,
min_guests INT NOT NULL,
max_guests INT NOT NULL,
total_price DECIMAL(12,2) NOT NULL,
CONSTRAINT fk_tier_package FOREIGN KEY (package_id) REFERENCES Package(package_id),
CONSTRAINT uq_tier_number UNIQUE (package_id, tier_number),
CONSTRAINT uq_tier_bracket UNIQUE (package_id, min_guests),
CONSTRAINT chk_tier_range CHECK (min_guests > 0 AND max_guests >= min_guests),
CONSTRAINT chk_tier_price CHECK (total_price > 0)
);

CREATE TABLE Menu (
menu_id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(50) NOT NULL,
price_per_guest DECIMAL(10,2) NOT NULL,
CONSTRAINT uq_menu_name UNIQUE (name),
CONSTRAINT chk_menu_price CHECK (price_per_guest > 0)
);

CREATE TABLE AddOn(
addon_id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(100) NOT NULL,
price DECIMAL(10,2) NOT NULL,
included_in_package BOOLEAN NOT NULL DEFAULT FALSE,
CONSTRAINT uq_addon_name UNIQUE (name),
CONSTRAINT chk_addon_price CHECK (price >= 0)
);

CREATE TABLE DecorOption (
decor_id INT PRIMARY KEY AUTO_INCREMENT,
category VARCHAR(50)NOT NULL,
name VARCHAR(100) NOT NULL,
price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
CONSTRAINT uq_decor UNIQUE (category, name),
CONSTRAINT chk_decor_price CHECK (price >= 0)
);

CREATE TABLE BookingAddOn (
booking_id INT NOT NULL,
addon_id INT NOT NULL,
quantity INT NOT NULL DEFAULT 1,
PRIMARY KEY (booking_id, addon_id),
CONSTRAINT fk_bookingaddon_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
CONSTRAINT fk_bookingaddon_addon FOREIGN KEY (addon_id) REFERENCES AddOn(addon_id),
CONSTRAINT chk_bookingaddon_qty CHECK (quantity > 0)
);

CREATE TABLE BookingDecorSelection (
booking_id INT NOT NULL,
decor_id INT NOT NULL,
quantity INT NOT NULL DEFAULT 1,
PRIMARY KEY (booking_id, decor_id),
CONSTRAINT fk_bookingdecor_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
CONSTRAINT fk_bookingdecor_decor FOREIGN KEY (decor_id) REFERENCES DecorOption(decor_id),
CONSTRAINT chk_bookingdecor_qty CHECK (quantity > 0)
);

CREATE TABLE Staff (
staff_id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(100) NOT NULL,
role VARCHAR(50) NOT NULL,
type ENUM('Internal','Vendor') NOT NULL,
contact VARCHAR(100) NULL
);

CREATE TABLE StaffAssignment (
booking_id INT NOT NULL,
staff_id INT NOT NULL,
assigned_date DATE NOT NULL,
PRIMARY KEY (booking_id, staff_id),
CONSTRAINT fk_assignment_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
CONSTRAINT fk_assignment_staff FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
);

CREATE TABLE Payment (
payment_id INT PRIMARY KEY AUTO_INCREMENT,
booking_id INT NOT NULL,
amount DECIMAL(12,2) NOT NULL,
payment_type ENUM('Deposit','Balance') NOT NULL,
payment_date DATE NOT NULL,
payment_status ENUM('Pending','Partially Paid','Fully Paid') NOT NULL DEFAULT 'Pending',
refund_status ENUM('N/A','Forfeited','Refund Due','Refunded') NOT NULL DEFAULT 'N/A',
CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
CONSTRAINT chk_payment_amount CHECK (amount > 0)
);

CREATE TABLE Invoice (
invoice_id INT PRIMARY KEY AUTO_INCREMENT,
booking_id INT NOT NULL,
total_amount DECIMAL(12,2) NOT NULL,
issue_date DATE NOT NULL,
CONSTRAINT fk_invoice_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
CONSTRAINT uq_invoice_booking UNIQUE (booking_id),
CONSTRAINT chk_invoice_total CHECK (total_amount >= 0)
);

CREATE TABLE WeddingBooking (
booking_id INT PRIMARY KEY,
package_id INT NOT NULL,
CONSTRAINT fk_weddingbooking_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
CONSTRAINT fk_weddingbooking_package FOREIGN KEY (package_id) REFERENCES Package(package_id)
);

CREATE TABLE OtherEventBooking (
booking_id INT PRIMARY KEY,
menu_id INT NOT NULL,
CONSTRAINT fk_otherevent_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
CONSTRAINT fk_otherevent_menu FOREIGN KEY (menu_id) REFERENCES Menu(menu_id)
);

-- POPULATING THE DATA

INSERT INTO Customer (customer_id, name, phone, email) VALUES
(1,'Nadeesha Perera','0771234501','nadeesha.perera@example.com'),
(2,'Kasun Wickramasinghe','0712345602','kasun.w@example.com'),
(3,'Ishara Gunawardena','0763456703','ishara.g@example.com'),
(4,'Tharindu Rajapaksha','0754567804','tharindu.r@example.com'),
(5,'Sanduni Herath','0778901205','sanduni.h@example.com'),
(6,'Chamath Dissanayake','0719012306','chamath.d@example.com'),
(7,'Hiruni Fernando','0702345607','hiruni.f@example.com'),
(8,'Ruwan Jayawardena','0725678908','ruwan.j@example.com');

INSERT INTO Booking (booking_id, customer_id, event_date, event_type, guest_count, status, total_price, venue_damaged) VALUES
(1, 1,'2026-08-15','Wedding',150,'Completed',590000.00,FALSE),
(2, 2,'2026-09-05','Other',120,'Completed',465400.00,TRUE ),
(3, 3,'2026-09-19','Other',80,'Cancelled',263000.00, FALSE),
(4, 4,'2026-10-24','Wedding',250,'Confirmed',815000.00,FALSE),
(5, 5,'2026-11-14','Other',200,'Confirmed',779000.00,FALSE),
(6, 6,'2026-12-12','Wedding',300,'Confirmed',970000.00,FALSE),
(7, 7,'2027-01-09','Wedding',180,'Pending',630000.00, FALSE),
(8, 8,'2026-11-28','Other',160,'Pending',801200.00,FALSE),
(9, 1,'2027-02-20','Other',150,'Pending',453000.00,FALSE),
(10, 3,'2026-10-03','Other',100,'Confirmed',380000.00,FALSE);

INSERT INTO Package (package_id, name, description) VALUES
(1, 'Wedding Package 1', 'All-inclusive wedding package: A/C hall, decor, DJ, dancing team, Jaya Mangala Gatha, Milk Fountain and Package Menu 01.'),
    
(2, 'Wedding Package 2', 'All-inclusive wedding package: A/C hall, decor, DJ, dancing team, Jaya Mangala Gatha, Milk Fountain and Package Menu 02.'),
    
(3, 'Wedding Package 3', 'All-inclusive wedding package: A/C hall, decor, DJ, dancing team, Jaya Mangala Gatha, Milk Fountain and Package Menu 03.'),
    
(4, 'Wedding Package 4', 'All-inclusive wedding package: A/C hall, decor, DJ, dancing team, Jaya Mangala Gatha, Milk Fountain and Package Menu 04.');

INSERT INTO PackagePricingTier (tier_id, package_id, tier_number, min_guests, max_guests, total_price) VALUES
(1, 1, 1, 100, 150, 530000.00),
(2, 1, 2, 151, 200, 630000.00),
(3, 1, 3, 201, 250, 730000.00),
(4, 1, 4, 251, 300, 830000.00),

(5, 2, 1, 100, 150, 590000.00),
(6, 2, 2, 151, 200, 670000.00),
(7, 2, 3, 201, 250, 750000.00),
(8, 2, 4, 251, 300, 860000.00),

(9, 3, 1, 100, 150, 625000.00),
(10, 3, 2, 151, 200, 720000.00),
(11, 3, 3, 201, 250, 815000.00),
(12, 3, 4, 251, 300, 910000.00),

(13, 4, 1, 100, 150, 670000.00),
(14, 4, 2, 151, 200, 770000.00),
(15, 4, 3, 201, 250, 870000.00),
(16, 4, 4, 251, 300, 970000.00);

INSERT INTO Menu (menu_id, name, price_per_guest) VALUES
(1,'Menu 01', 2850.00),
(2,'Menu 02', 3250.00),
(3,'Menu 03', 3650.00),
(4,'Menu 04', 4400.00);

INSERT INTO AddOn (addon_id, name, price,included_in_package) VALUES
(1,'Iced Coffee Service',170.00,FALSE), -- 170 per guest
(2,'Bar Staff', 3000.00,FALSE),   -- 3000 per waiter
(3,'DJ Music with Lights',25000.00,TRUE),
(4,'Outdoor Lightings', 20000.00,TRUE);

INSERT INTO DecorOption (decor_id, category, name,price) VALUES
(1,'Settee Back','Classic White Floral with Drapes',25000.00),
(2,'Settee Back','Red Rose Circular Floral Backdrop',35000.00),
(3,'Settee Back','White Rose Circular Floral Backdrop',35000.00),
(4,'Poruwa','Modern White Pillar Structure',40000.00),
(5,'Poruwa','Traditional Outdoor Floral Setup',45000.00),
(6,'Entrance Decor','Floral Welcome Arch with Custom Signage',15000.00),
(7,'Entrance Decor','Rustic Wooden Entrance Arch',20000.00),
(8,'Table Decoration','Tall Blue Glass Floral Centerpiece',2000.00),
(9,'Color Theme','Red & Dark Contrast',0.00),
(10,'Color Theme','White & Gold',0.00);

INSERT INTO BookingAddOn (booking_id, addon_id, quantity) VALUES
(2, 1,120), (5, 2, 1), (8, 1, 160), (8, 3, 1), (9, 1, 150), (10, 4, 1);

INSERT INTO BookingDecorSelection (booking_id, decor_id,quantity) VALUES
(1, 1,1), (1, 3, 1),(2, 7,1 ),(4, 2, 1), (4, 4, 1), (4, 6, 1),(5, 8, 23),(6, 1, 1), (6, 5, 1),(7, 2, 1),(8, 5, 1);

INSERT INTO Staff (staff_id, name, role, type, contact) VALUES
(1,'Nimal Senanayake','Event Coordinator','Internal','0771100001'),
(2,'Kasun Bandara','Head Waiter','Internal','0771100002'),
(3,'Chamari Jayasinghe','Waitress','Internal','0771100003'),
(4,'Ruwan Samarasinghe','Head Cook','Internal','0771100004'),
(5,'Sampath Rathnayake','Security','Internal','0771100005'),
(6,'Dilan Wijesekara','DJ','Vendor','0771100006'),
(7,'Udara Flora & Decor','Decorator','Vendor','0771100007'),
(8,'Malith Gunasekara','Waiter','Internal','0771100008'),
(9,'Helakala Mandapaya','Dancing Group','Vendor','0772200001'),
(10,'Helakala Mandapaya','Jaya Mangala Gatha','Vendor','0772200002');

INSERT INTO StaffAssignment (booking_id, staff_id, assigned_date) VALUES
(1, 1,'2026-08-15'),(1, 2,'2026-08-15'),(1, 3,'2026-08-15'),
(1,7, '2026-08-15'),(1, 9,'2026-08-15'),(1,10,'2026-08-15'),
(2,1,'2026-09-05'),(2,2,'2026-09-05'),(2,3,'2026-09-05'), (2, 4,'2026-09-05'),
(4,1,'2026-10-24'),(4,2,'2026-10-24'),(4, 6,'2026-10-24'),(4, 7,'2026-10-24'),
(4,8,'2026-10-24'),(4,9,'2026-10-24'),(4,10,'2026-10-24'),
(5,1,'2026-11-14'),(5,3,'2026-11-14'),(5, 5,'2026-11-14'), (5, 8,'2026-11-14'),
(6, 1,'2026-12-12'),(6,2, '2026-12-12'), (6, 6,'2026-12-12'),
(6,7,'2026-12-12'), (6,9,'2026-12-12'),(6,10,'2026-12-12');

INSERT INTO Payment (payment_id, booking_id, amount, payment_type, payment_date, payment_status, refund_status) VALUES
(1,1,20000.00,'Deposit','2026-06-01','Partially Paid','Refunded'),
(2,1,570000.00,'Balance','2026-08-01','Fully Paid','N/A'),
(3,2,20000.00,'Deposit','2026-07-10','Partially Paid','Forfeited'),  -- venue damaged
(4,2,445400.00,'Balance','2026-08-22','Fully Paid','N/A'),
(5,3,20000.00,'Deposit','2026-08-20','Partially Paid','Forfeited'),  -- event cancelled
(6,4,20000.00,'Deposit','2026-07-15','Partially Paid','N/A'),
(7,5,20000.00,'Deposit','2026-09-01','Partially Paid','N/A'),
(8,6,20000.00,'Deposit','2026-08-30','Partially Paid','N/A'),
(9,10,20000.00,'Deposit','2026-09-10','Partially Paid','N/A'),
(10,10,360000.00,'Balance','2026-09-19','Fully Paid','N/A');

INSERT INTO Invoice (invoice_id, booking_id, total_amount, issue_date) VALUES
(1,1,590000.00,'2026-07-25'),
(2,2,465400.00,'2026-08-15'),
(3,4,815000.00,'2026-09-20'),
(4,5,779000.00,'2026-09-22'),
(5,6,970000.00,'2026-09-15'),
(6,10,380000.00,'2026-09-12');

INSERT INTO WeddingBooking (booking_id, package_id) VALUES
(1, 2), (4, 3), (6, 4), (7, 1);

INSERT INTO OtherEventBooking (booking_id, menu_id) VALUES
(2, 2), (3, 1), (5, 3), (8, 4), (9, 1), (10, 2);

-- QUERIES

-- Dinuji S.A.P./ IT25103187

-- 1. Which Addon_id has been attached to the most bookings and how many timea it's been choosen?
SELECT addon_id, COUNT(*) AS times_chosen
FROM BookingAddOn
GROUP BY addon_id
ORDER BY times_chosen DESC
LIMIT 1;

-- 2. Which addon is the most expensive?
SELECT addon_id, name, price
FROM AddOn
ORDER BY price DESC
LIMIT 1;

-- 3. Which booking has selected the most decorations?
SELECT booking_id, COUNT(*) AS decorations_selected
FROM BookingDecorSelection
GROUP BY booking_id
ORDER BY decorations_selected DESC
Limit 1;

-- 4. How many decorations are available per category and what is their average price?
SELECT category, COUNT(*) AS decoration_count, 
ROUND(AVG(price), 2) AS average_price
FROM DecorOption
GROUP BY category
ORDER BY decoration_count DESC;

-- Kawmadee R.L.D / IT25104035

-- 1.Which upcoming bookings are scheduled, and who are the customers?
SELECT b.booking_id, b.event_date, b.event_type, b.guest_count, b.status, c.name AS customer, c.phone
FROM Booking b
JOIN Customer c ON b.customer_id = c.customer_id WHERE b.status IN ('Pending', 'Confirmed') AND b.event_date >= CURDATE()
ORDER BY b.event_date;

-- 2.Which bookings have between 150 and 300 guests?
SELECT booking_id, event_date, event_type, guest_count,status
FROM Booking
WHERE guest_count BETWEEN 150 AND 300
ORDER BY guest_count DESC;

-- 3.Which booking has the highest number of guests?
SELECT booking_id, event_date, event_type, guest_count, status
FROM Booking
WHERE guest_count = (SELECT MAX(guest_count)FROM Booking);

-- 4.What is the price per guest for each booking?
SELECT booking_id, event_type, guest_count, total_price, ROUND(total_price / guest_count, 2) AS price_per_guest
FROM Booking
ORDER BY price_per_guest DESC;


-- Fernando W.S.H / IT25102808

-- 1. All fully paid payments
SELECT SUM(amount) AS total_paid_payments
FROM Payment
WHERE payment_status ='Fully Paid'; 

-- 2. List all payments with customer name and event date
SELECT p.payment_id, c.name AS customer_name, b.event_date,
       p.payment_type, p.amount, p.payment_status
FROM Payment p
JOIN Booking b ON p.booking_id = b.booking_id
JOIN Customer c ON b.customer_id = c.customer_id
ORDER BY b.event_date;

-- 3. Find bookings where the balance is due within 14 days of the event (but the balance hasn't been paid yet)
SELECT b.booking_id, c.name AS customer_name, b.event_date,
       DATE_SUB(b.event_date, INTERVAL 14 DAY) AS balance_due_date
FROM Booking b
JOIN Customer c ON b.customer_id = c.customer_id
WHERE b.status = 'Confirmed'
  AND b.booking_id NOT IN (
      SELECT booking_id FROM Payment WHERE payment_type = 'Balance'
  );

-- 4. List all invoices with the total amount, sorted highest first
SELECT i.invoice_id, c.name AS customer_name, i.total_amount, i.issue_date
FROM Invoice i
JOIN Booking b ON i.booking_id = b.booking_id
JOIN Customer c ON b.customer_id = c.customer_id
ORDER BY i.total_amount DESC;

-- Perera L.D.U.J. / IT25300121

-- 1. Which staff members are available on a given date?
SELECT staff_id, name, role, type, contact
FROM Staff
WHERE staff_id NOT IN (SELECT staff_id FROM StaffAssignment
WHERE assigned_date = '2026-10-24')
ORDER BY role, name;

-- 2. Which staff members are assigned to a confirmed booking?

SELECT staff_id, name, role, type,
(SELECT GROUP_CONCAT(booking_id ORDER BY booking_id SEPARATOR ', ')
FROM StaffAssignment
WHERE StaffAssignment.staff_id = Staff.staff_id
AND booking_id IN (SELECT booking_id FROM Booking
WHERE status = 'Confirmed')) AS booking_ids FROM Staff
WHERE staff_id IN (SELECT staff_id FROM StaffAssignment
WHERE booking_id IN (SELECT booking_id FROM Booking
WHERE status = 'Confirmed'))
ORDER BY role, name;



-- 3. Which staff members are unavailable on a particular date?
SELECT staff_id, name, role, type
FROM Staff
WHERE staff_id IN (SELECT staff_id FROM StaffAssignment
WHERE assigned_date = '2026-10-24')
ORDER BY role, name;

-- 4. Which staff members are assigned to more than one event?
SELECT staff_id, name, role,
(SELECT COUNT(*) FROM StaffAssignment
WHERE StaffAssignment.staff_id = Staff.staff_id) AS total_assignments,
(SELECT GROUP_CONCAT(booking_id ORDER BY booking_id SEPARATOR ', ')
FROM StaffAssignment WHERE StaffAssignment.staff_id = Staff.staff_id) AS booking_ids
FROM Staff WHERE (SELECT COUNT(*)FROM StaffAssignment
WHERE StaffAssignment.staff_id = Staff.staff_id) > 1
ORDER BY total_assignments DESC, staff_id;