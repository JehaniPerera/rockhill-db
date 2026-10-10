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


