-- 1. CATALOG (Function 2)

-- Wedding packages
INSERT IGNORE INTO `package` (package_id, name, description) VALUES
(1, 'Wedding Package 1', 'All-inclusive wedding package: A/C hall, decor, DJ, dancing team, Jaya Mangala Gatha, Milk Fountain and Package Menu 01.'),
(2, 'Wedding Package 2', 'All-inclusive wedding package: A/C hall, decor, DJ, dancing team, Jaya Mangala Gatha, Milk Fountain and Package Menu 02.'),
(3, 'Wedding Package 3', 'All-inclusive wedding package: A/C hall, decor, DJ, dancing team, Jaya Mangala Gatha, Milk Fountain and Package Menu 03.'),
(4, 'Wedding Package 4', 'All-inclusive wedding package: A/C hall, decor, DJ, dancing team, Jaya Mangala Gatha, Milk Fountain and Package Menu 04.');

-- Flat total price per guest-count bracket (wedding packages)
INSERT IGNORE INTO package_pricing_tier (tier_id, package_id, tier_number, min_guests, max_guests, total_price) VALUES
(1,  1, 1, 100, 150, 530000.00),
(2,  1, 2, 151, 200, 630000.00),
(3,  1, 3, 201, 250, 730000.00),
(4,  1, 4, 251, 300, 830000.00),

(5,  2, 1, 100, 150, 590000.00),
(6,  2, 2, 151, 200, 670000.00),
(7,  2, 3, 201, 250, 750000.00),
(8,  2, 4, 251, 300, 860000.00),

(9,  3, 1, 100, 150, 625000.00),
(10, 3, 2, 151, 200, 720000.00),
(11, 3, 3, 201, 250, 815000.00),
(12, 3, 4, 251, 300, 910000.00),

(13, 4, 1, 100, 150, 670000.00),
(14, 4, 2, 151, 200, 770000.00),
(15, 4, 3, 201, 250, 870000.00),
(16, 4, 4, 251, 300, 970000.00);

-- Catering menus for non-wedding events (price per guest)
INSERT IGNORE INTO menu (menu_id, name, price_per_guest) VALUES
(1, 'Menu 01', 2850.00),
(2, 'Menu 02', 3250.00),
(3, 'Menu 03', 3650.00),
(4, 'Menu 04', 4400.00);

-- Add-ons
INSERT IGNORE INTO add_on (addon_id, name, price, included_in_package, unit_type, is_wedding_exclusive) VALUES
(1, 'Iced Coffee Service',  170.00,   0, 'per guest', 0),
(2, 'Bar Staff',            3000.00,  0, 'per item',  0),
(3, 'DJ Music with Lights', 25000.00, 1, 'per item',  0),
(4, 'Outdoor Lightings',    20000.00, 1, 'per item',  0),
(5, 'Jayamangala Gatha',    12000.00, 1, 'per item',  1);

-- Decor options (is_singular = 1: only one per category; 0: quantity = tables)
INSERT IGNORE INTO decor_option (decor_id, category, name, price, is_singular) VALUES
(1,  'Settee Back',      'Classic White Floral with Drapes',        25000.00, 1),
(2,  'Settee Back',      'Red Rose Circular Floral Backdrop',       35000.00, 1),
(3,  'Settee Back',      'White Rose Circular Floral Backdrop',     35000.00, 1),
(4,  'Poruwa',           'Modern White Pillar Structure',           40000.00, 1),
(5,  'Poruwa',           'Traditional Outdoor Floral Setup',        45000.00, 1),
(6,  'Entrance Decor',   'Floral Welcome Arch with Custom Signage', 15000.00, 1),
(7,  'Entrance Decor',   'Rustic Wooden Entrance Arch',             20000.00, 1),
(8,  'Table Decoration', 'Tall Blue Glass Floral Centerpiece',      2000.00,  0),
(9,  'Color Theme',      'Red & Dark Contrast',                     0.00,     1),
(10, 'Color Theme',      'White & Gold',                            0.00,     1);

-- 2. CUSTOMERS AND BOOKINGS (Function 1, Kawmadee)
-- NOTE: the customer table's key column is `id` (JPA entity Customer), not customer_id.

-- customers
INSERT IGNORE INTO customer (id, name, phone, email) VALUES
(1,'Nadeesha Perera','0771234501','nadeesha.perera@example.com'),
(2,'Kasun Wickramasinghe','0712345602','kasun.w@example.com'),
(3,'Ishara Gunawardena','0763456703','ishara.g@example.com'),
(4,'Tharindu Rajapaksha','0754567804','tharindu.r@example.com'),
(5,'Sanduni Herath','0778901205','sanduni.h@example.com'),
(6,'Chamath Dissanayake','0719012306','chamath.d@example.com'),
(7,'Hiruni Fernando','0702345607','hiruni.f@example.com'),
(8,'Ruwan Jayawardena','0725678908','ruwan.j@example.com');

-- bookings (parent table)
INSERT IGNORE INTO booking (booking_id, customer_id, event_date, event_type, guest_count, status, submitted,
    venue_damaged, venue_damage_notes, cancellation_reason, confirmed_at, deposit_received_at,
    deposit_confirmed_by, last_activity_at) VALUES
(1, 1,'2026-08-15','WEDDING',150,'COMPLETED',1,0,NULL,NULL,'2026-05-30 10:00:00','2026-06-01 10:00:00','AUTO',NOW()),
(2, 2,'2026-09-05','OTHER',120,'COMPLETED',1,1,'Broken glass partition and stained carpet',NULL,'2026-07-08 10:00:00','2026-07-10 10:00:00','AUTO',NOW()),
(3, 3,'2026-09-19','OTHER',80,'CANCELLED',1,0,NULL,'Customer cancelled the event','2026-08-18 10:00:00','2026-08-20 10:00:00','AUTO',NOW()),
(4, 4,'2026-10-24','WEDDING',250,'CONFIRMED',1,0,NULL,NULL,'2026-07-13 10:00:00','2026-07-15 10:00:00','AUTO',NOW()),
(5, 5,'2026-11-14','OTHER',200,'CONFIRMED',1,0,NULL,NULL,'2026-08-30 10:00:00','2026-09-01 10:00:00','AUTO',NOW()),
(6, 6,'2026-12-12','WEDDING',300,'CONFIRMED',1,0,NULL,NULL,'2026-08-28 10:00:00','2026-08-30 10:00:00','AUTO',NOW()),
(7, 7,'2027-01-09','WEDDING',180,'PENDING',1,0,NULL,NULL,NULL,NULL,NULL,NOW()),
(8, 8,'2026-11-28','OTHER',160,'PENDING',1,0,NULL,NULL,NULL,NULL,NULL,NOW()),
(9, 1,'2027-02-20','OTHER',150,'PENDING',1,0,NULL,NULL,NULL,NULL,NULL,NOW()),
(10,3,'2026-10-03','OTHER',100,'COMPLETED',1,0,NULL,NULL,'2026-09-08 10:00:00','2026-09-10 10:00:00','AUTO',NOW());

-- wedding bookings (package ids come from Dinuji's catalog)
INSERT IGNORE INTO wedding_booking (booking_id, package_id) VALUES
(1,2),(4,3),(6,4),(7,1);

-- other (non-wedding) event bookings (menu ids come from Dinuji's catalog)
INSERT IGNORE INTO other_event_booking (booking_id, menu_id, event_description) VALUES
(2,2,'Corporate dinner'),
(3,1,'Birthday party'),
(5,3,'Anniversary celebration'),
(8,4,'Graduation reception'),
(9,1,'Family get-together'),
(10,2,'Engagement party');


-- 3. ADD-ON AND DECOR CHOICES (Function 2, Dinuji)

-- Sample add-on and decor choices for three of the bookings above, so the price
-- estimate has something to show. charged_amount is the price at the time of
-- choosing: weddings pay 0 for decor and for add-ons included in the package;
-- other events pay full price. Table decor quantity = guests / 9 rounded up.
INSERT IGNORE INTO booking_add_on (booking_id, addon_id, quantity, charged_amount) VALUES
(4, 1, 250, 170.00),   -- wedding, 250 guests: iced coffee per guest (not included, so charged)
(4, 3, 1,   0.00),     --   DJ (included in package)
(4, 4, 1,   0.00),     --   outdoor lightings (included)
(4, 5, 1,   0.00),     --   jayamangala gatha (included)
(5, 1, 200, 170.00),   -- other event, 200 guests
(5, 2, 1,   3000.00),
(8, 1, 160, 170.00),   -- other event, 160 guests
(8, 2, 1,   3000.00);

INSERT IGNORE INTO booking_decor_selection (booking_id, decor_id, quantity, charged_amount) VALUES
(4, 2,  1,  0.00),     -- wedding: decor is free
(4, 4,  1,  0.00),
(4, 6,  1,  0.00),
(4, 8,  28, 0.00),     -- 250 guests / 9 = 28 tables
(4, 10, 1,  0.00),
(5, 6,  1,  15000.00), -- other event, 200 guests
(5, 8,  23, 2000.00),  -- 200 / 9 = 23 tables
(5, 9,  1,  0.00),
(8, 7,  1,  20000.00), -- other event, 160 guests
(8, 8,  18, 2000.00),  -- 160 / 9 = 18 tables
(8, 10, 1,  0.00);


-- 6. PAYMENTS AND INVOICES (Function 4, Fernando)
-- The tables are created by schema.sql, which runs before this file.
-- The bookings used here (1, 2, 3, 4, 5, 6, 10) come from section 2.
INSERT IGNORE INTO Payment (payment_id, booking_id, amount, payment_type, payment_date, payment_status, refund_status, refund_date) VALUES
(1, 1, 20000.00,  'Deposit', '2026-06-01', 'Partially Paid', 'Refunded',  '2026-08-16'),
(2, 1, 590000.00, 'Balance', '2026-08-01', 'Fully Paid',     'N/A',       NULL),
(3, 2, 20000.00,  'Deposit', '2026-07-10', 'Partially Paid', 'Forfeited', '2026-09-06'),
(4, 2, 465400.00, 'Balance', '2026-08-22', 'Fully Paid',     'N/A',       NULL),
(5, 3, 20000.00,  'Deposit', '2026-08-20', 'Partially Paid', 'Forfeited', '2026-09-05'),
(6, 4, 20000.00,  'Deposit', '2026-07-15', 'Partially Paid', 'N/A',       NULL),
(7, 5, 20000.00,  'Deposit', '2026-09-01', 'Partially Paid', 'N/A',       NULL),
(8, 6, 20000.00,  'Deposit', '2026-08-30', 'Partially Paid', 'N/A',       NULL),
(9, 10, 20000.00, 'Deposit', '2026-09-10', 'Partially Paid', 'Refunded',  '2026-10-04'),
(10, 10, 380000.00, 'Balance', '2026-09-19', 'Fully Paid',   'N/A',       NULL);

INSERT IGNORE INTO Invoice (invoice_id, booking_id, total_amount, issue_date) VALUES
(1, 1, 590000.00, '2026-08-01'),
(2, 2, 465400.00, '2026-08-22'),
(3, 10, 380000.00, '2026-09-19');

-- demo payment proofs: one waiting for approval, one rejected
INSERT IGNORE INTO PaymentProof (proof_id, booking_id, payment_type, amount, bank_reference, file_name, content_type, status, rejection_reason, submitted_at, reviewed_at) VALUES
(1, 4, 'Balance', 815000.00, 'BT-20261012-001', '43155aca-14db-4ec6-a7c5-f41105912cdc.png', 'image/png', 'Pending Approval', NULL, '2026-10-12 14:30:00', NULL),
(2, 6, 'Balance', 970000.00, 'BT-20261010-044', 'missing-demo-file.png', 'image/png', 'Rejected', 'Amount on the slip does not match the balance due. Please upload the correct slip.', '2026-10-10 09:15:00', '2026-10-10 16:40:00');
