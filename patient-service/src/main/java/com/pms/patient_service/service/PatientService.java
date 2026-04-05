package com.pms.patient_service.service;

import com.pms.patient_service.dto.PatientRequestDTO;
import com.pms.patient_service.dto.PatientResponseDTO;
import com.pms.patient_service.exception.EmailAlreadyExistsException;
import com.pms.patient_service.exception.PatientNotFoundException;
import com.pms.patient_service.grpc.BillingServiceGrpcClient;
import com.pms.patient_service.kafka.KafkaProducer;
import com.pms.patient_service.mapper.PatientMapper;
import com.pms.patient_service.model.Patient;
import com.pms.patient_service.repository.PatientRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient,  KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();
//        List<PatientResponseDTO> response = new ArrayList<>();
//        for(Patient patient : patients){
//            PatientResponseDTO patientResponse = PatientMapper.toPatientResponseDTO(patient);
//            response.add(patientResponse);
//        }

        //patient get passed by map
        List<PatientResponseDTO> response =
                patients.stream()
                        .map(PatientMapper::toPatientResponseDTO)
                        .toList();
        return response;
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("A patient with this email " + patientRequestDTO.getEmail() + " already exists ");
        }

        Patient newPatient = patientRepository.save(PatientMapper.toPatientModel(patientRequestDTO));
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());
        log.info("billing account created successfully");
        kafkaProducer.sendEvent(newPatient);
        log.info("Patient event created successfully");

        return PatientMapper.toPatientResponseDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with ID : " + id));
        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)){
            throw new EmailAlreadyExistsException("A patient with this email " + patientRequestDTO.getEmail() + " already exists ");
        }
        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toPatientResponseDTO(updatedPatient);
    }

    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}
