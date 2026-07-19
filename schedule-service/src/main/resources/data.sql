-- TimeSlots for schedule-service
-- Cardiology Doctors (DOC-001, DOC-002)
INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-001', 'DOC-001', 'Dr. John Smith', 
       CURRENT_DATE + INTERVAL '9 hours', CURRENT_DATE + INTERVAL '9 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-001');

INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-002', 'DOC-001', 'Dr. John Smith', 
       CURRENT_DATE + INTERVAL '10 hours', CURRENT_DATE + INTERVAL '10 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-002');

INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-003', 'DOC-002', 'Dr. Sarah Johnson', 
       CURRENT_DATE + INTERVAL '11 hours', CURRENT_DATE + INTERVAL '11 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-003');

-- Neurology Doctor (DOC-003)
INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-004', 'DOC-003', 'Dr. Michael Brown', 
       CURRENT_DATE + INTERVAL '14 hours', CURRENT_DATE + INTERVAL '14 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-004');

INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-005', 'DOC-003', 'Dr. Michael Brown', 
       CURRENT_DATE + INTERVAL '15 hours', CURRENT_DATE + INTERVAL '15 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-005');

-- Orthopedics Doctor (DOC-004)
INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-006', 'DOC-004', 'Dr. Emily Davis', 
       CURRENT_DATE + INTERVAL '9 hours', CURRENT_DATE + INTERVAL '9 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-006');

INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-007', 'DOC-004', 'Dr. Emily Davis', 
       CURRENT_DATE + INTERVAL '10 hours', CURRENT_DATE + INTERVAL '10 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-007');

-- Pediatrics Doctor (DOC-005)
INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-008', 'DOC-005', 'Dr. James Wilson', 
       CURRENT_DATE + INTERVAL '11 hours', CURRENT_DATE + INTERVAL '11 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-008');

INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-009', 'DOC-005', 'Dr. James Wilson', 
       CURRENT_DATE + INTERVAL '14 hours', CURRENT_DATE + INTERVAL '14 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-009');

INSERT INTO time_slots (id, time_slot_id, doctor_id, doctor_name, start_time, end_time)
SELECT gen_random_uuid(), 'TS-010', 'DOC-005', 'Dr. James Wilson', 
       CURRENT_DATE + INTERVAL '15 hours', CURRENT_DATE + INTERVAL '15 hours 30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM time_slots WHERE time_slot_id = 'TS-010');

-- Appointments
INSERT INTO appointments (id, appointment_id, patient_id, patient_name, patient_email, doctor_id, doctor_name, 
                         time_slot_id, status, created_at)
SELECT gen_random_uuid(), 'APT-001', 'PMS-001', 'John Doe', 'john.doe@example.com', 
       'DOC-001', 'Dr. John Smith', 'TS-001', 'BOOKED', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM appointments WHERE appointment_id = 'APT-001');

INSERT INTO appointments (id, appointment_id, patient_id, patient_name, patient_email, doctor_id, doctor_name, 
                         time_slot_id, status, created_at)
SELECT gen_random_uuid(), 'APT-002', 'PMS-002', 'Jane Smith', 'jane.smith@example.com', 
       'DOC-003', 'Dr. Michael Brown', 'TS-004', 'BOOKED', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM appointments WHERE appointment_id = 'APT-002');

INSERT INTO appointments (id, appointment_id, patient_id, patient_name, patient_email, doctor_id, doctor_name, 
                         time_slot_id, status, created_at)
SELECT gen_random_uuid(), 'APT-003', 'PMS-003', 'Alice Johnson', 'alice.johnson@example.com', 
       'DOC-004', 'Dr. Emily Davis', 'TS-006', 'BOOKED', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM appointments WHERE appointment_id = 'APT-003');