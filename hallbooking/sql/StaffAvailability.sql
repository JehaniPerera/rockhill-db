
USE rockhill;

CREATE TABLE StaffAvailability (
availability_id INT PRIMARY KEY AUTO_INCREMENT,
staff_id INT NOT NULL,
unavailable_date DATE NOT NULL,
reason VARCHAR(255) NULL,
CONSTRAINT fk_availability_staff FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE,
CONSTRAINT uq_availability_staff_date UNIQUE (staff_id, unavailable_date)
);

ALTER TABLE StaffAssignment
ADD CONSTRAINT uq_assignment_staff_date UNIQUE (staff_id, assigned_date);
