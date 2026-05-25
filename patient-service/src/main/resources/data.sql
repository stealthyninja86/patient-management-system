-- Patients (check by email since that's the unique constraint)
INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-001', 'John Doe', 'john.doe@example.com', '123 Main St, Springfield', '1985-06-15', '2024-01-10'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'john.doe@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-002', 'Jane Smith', 'jane.smith@example.com', '456 Elm St, Shelbyville', '1990-09-23', '2023-12-01'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'jane.smith@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-003', 'Alice Johnson', 'alice.johnson@example.com', '789 Oak St, Capital City', '1978-03-12', '2022-06-20'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'alice.johnson@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-004', 'Bob Brown', 'bob.brown@example.com', '321 Pine St, Springfield', '1982-11-30', '2023-05-14'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'bob.brown@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-005', 'Emily Davis', 'emily.davis@example.com', '654 Maple St, Shelbyville', '1995-02-05', '2024-03-01'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'emily.davis@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-006', 'Michael Green', 'michael.green@example.com', '987 Cedar St, Springfield', '1988-07-25', '2024-02-15'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'michael.green@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-007', 'Sarah Taylor', 'sarah.taylor@example.com', '123 Birch St, Shelbyville', '1992-04-18', '2023-08-25'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'sarah.taylor@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-008', 'David Wilson', 'david.wilson@example.com', '456 Ash St, Capital City', '1975-01-11', '2022-10-10'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'david.wilson@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-009', 'Laura White', 'laura.white@example.com', '789 Palm St, Springfield', '1989-09-02', '2024-04-20'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'laura.white@example.com');

INSERT INTO patient (id, patient_id, name, email, address, date_of_birth, registered_date)
SELECT gen_random_uuid(), 'PMS-010', 'James Harris', 'james.harris@example.com', '321 Cherry St, Shelbyville', '1993-11-15', '2023-06-30'
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE email = 'james.harris@example.com');