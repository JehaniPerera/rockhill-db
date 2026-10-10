-- Run in MySQL Workbench AFTER OOAD_Rockhill_Database.sql
-- (and add the same lines to the group's SQL file, because the class diagram has StaffAvailability).
USE rockhill;

CREATE TABLE StaffAvailability (
availability_id INT PRIMARY KEY AUTO_INCREMENT,
staff_id INT NOT NULL,
unavailable_date DATE NOT NULL,
reason VARCHAR(255) NULL,
CONSTRAINT fk_availability_staff FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE,
CONSTRAINT uq_availability_staff_date UNIQUE (staff_id, unavailable_date)
);

-- A staff member/vendor can never be on two events on the same date (also stops
-- two admins assigning the same person at the same moment).
-- The existing sample data has no clashes, so this is safe to add.
ALTER TABLE StaffAssignment
ADD CONSTRAINT uq_assignment_staff_date UNIQUE (staff_id, assigned_date);
