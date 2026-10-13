USE rockhill;

-- Hall damage check done by staff admin AFTER an event (Booking status = Completed).
-- The Payment function reads this table to decide whether the extra 20,000 deposit is returned.
CREATE TABLE HallDamageReport (
report_id INT PRIMARY KEY AUTO_INCREMENT,
booking_id INT NOT NULL,
is_damaged BOOLEAN NOT NULL,
notes VARCHAR(500) NULL,
reported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT fk_damage_booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
CONSTRAINT uq_damage_booking UNIQUE (booking_id)
);
