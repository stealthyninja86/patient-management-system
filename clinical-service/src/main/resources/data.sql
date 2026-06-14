-- Drugs for clinical-service
INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-001', 'Amoxicillin', '500mg', 'Antibiotic for bacterial infections', '3 times daily with food', 'TABLET'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-001');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-002', 'Ibuprofen', '400mg', 'NSAID for pain and inflammation', 'As needed for pain, max 3 daily', 'TABLET'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-002');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-003', 'Metformin', '850mg', 'Diabetes medication', 'Twice daily with meals', 'TABLET'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-003');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-004', 'Lisinopril', '10mg', 'ACE inhibitor for hypertension', 'Once daily in the morning', 'TABLET'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-004');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-005', 'Salbutamol', '100mcg', 'Bronchodilator for asthma', 'As needed for wheezing', 'INJECTION'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-005');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-006', 'Betamethasone', '0.05%', 'Topical steroid for skin conditions', 'Apply thin layer twice daily', 'OINTMENT'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-006');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-007', 'Ketoconazole', '2%', 'Antifungal shampoo', 'Use twice weekly', 'SHAMPOO'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-007');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-008', 'Calamine', '5%', 'Calamine lotion for itching', 'Apply topically as needed', 'LOTION'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-008');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-009', 'Paracetamol', '500mg', 'Pain reliever and fever reducer', 'Every 4-6 hours as needed', 'TABLET'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-009');

INSERT INTO drug (id, drug_id, name, dosage, description, usage, type)
SELECT gen_random_uuid(), 'DRUG-010', 'Omeprazole', '20mg', 'Proton pump inhibitor for acid reflux', 'Once daily before breakfast', 'TABLET'
WHERE NOT EXISTS (SELECT 1 FROM drug WHERE drug_id = 'DRUG-010');