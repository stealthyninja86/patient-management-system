-- Hospitals
INSERT INTO hospital (id, hospital_id, name, address)
SELECT gen_random_uuid(), 'HOSP-001', 'City General Hospital', '123 Main Street, Cityville'
WHERE NOT EXISTS (SELECT 1 FROM hospital WHERE hospital_id = 'HOSP-001');

INSERT INTO hospital (id, hospital_id, name, address)
SELECT gen_random_uuid(), 'HOSP-002', 'Lakeside Medical Center', '456 Lake Drive, Lakeside'
WHERE NOT EXISTS (SELECT 1 FROM hospital WHERE hospital_id = 'HOSP-002');

-- Departments
INSERT INTO department (id, department_id, name, hospital_id)
SELECT gen_random_uuid(), 'DEPT-001', 'Cardiology', h.id
FROM hospital h WHERE h.hospital_id = 'HOSP-001'
AND NOT EXISTS (SELECT 1 FROM department WHERE department_id = 'DEPT-001');

INSERT INTO department (id, department_id, name, hospital_id)
SELECT gen_random_uuid(), 'DEPT-002', 'Neurology', h.id
FROM hospital h WHERE h.hospital_id = 'HOSP-001'
AND NOT EXISTS (SELECT 1 FROM department WHERE department_id = 'DEPT-002');

INSERT INTO department (id, department_id, name, hospital_id)
SELECT gen_random_uuid(), 'DEPT-003', 'Orthopedics', h.id
FROM hospital h WHERE h.hospital_id = 'HOSP-002'
AND NOT EXISTS (SELECT 1 FROM department WHERE department_id = 'DEPT-003');

INSERT INTO department (id, department_id, name, hospital_id)
SELECT gen_random_uuid(), 'DEPT-004', 'Pediatrics', h.id
FROM hospital h WHERE h.hospital_id = 'HOSP-002'
AND NOT EXISTS (SELECT 1 FROM department WHERE department_id = 'DEPT-004');

-- Doctors
INSERT INTO doctor (id, doctor_id, name, department_id)
SELECT gen_random_uuid(), 'DOC-001', 'John Smith', d.id
FROM department d WHERE d.department_id = 'DEPT-001'
AND NOT EXISTS (SELECT 1 FROM doctor WHERE doctor_id = 'DOC-001');

INSERT INTO doctor (id, doctor_id, name, department_id)
SELECT gen_random_uuid(), 'DOC-002', 'Sarah Johnson', d.id
FROM department d WHERE d.department_id = 'DEPT-001'
AND NOT EXISTS (SELECT 1 FROM doctor WHERE doctor_id = 'DOC-002');

INSERT INTO doctor (id, doctor_id, name, department_id)
SELECT gen_random_uuid(), 'DOC-003', 'Michael Brown', d.id
FROM department d WHERE d.department_id = 'DEPT-002'
AND NOT EXISTS (SELECT 1 FROM doctor WHERE doctor_id = 'DOC-003');

INSERT INTO doctor (id, doctor_id, name, department_id)
SELECT gen_random_uuid(), 'DOC-004', 'Emily Davis', d.id
FROM department d WHERE d.department_id = 'DEPT-003'
AND NOT EXISTS (SELECT 1 FROM doctor WHERE doctor_id = 'DOC-004');

INSERT INTO doctor (id, doctor_id, name, department_id)
SELECT gen_random_uuid(), 'DOC-005', 'James Wilson', d.id
FROM department d WHERE d.department_id = 'DEPT-004'
AND NOT EXISTS (SELECT 1 FROM doctor WHERE doctor_id = 'DOC-005');
