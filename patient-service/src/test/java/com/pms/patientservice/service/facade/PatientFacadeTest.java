package com.pms.patientservice.service.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.patientservice.dto.request.PatientRequestDTO;
import com.pms.patientservice.dto.response.PatientResponseDTO;
import com.pms.patientservice.exception.EmailAlreadyExistsException;
import com.pms.patientservice.grpc.BillingGrpcClient;
import com.pms.patientservice.model.OutboxEvent;
import com.pms.patientservice.model.Patient;
import com.pms.patientservice.repository.OutboxRepository;
import com.pms.patientservice.repository.PatientRepository;
import com.pms.patientservice.service.PatientService;
import com.pms.patientservice.service.mapper.PatientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientFacadeTest {

    @Mock private PatientService patientService;
    @Mock private PatientRepository patientRepository;
    @Mock private OutboxRepository outboxRepository;
    @Mock private BillingGrpcClient billingGrpcClient;
    @Mock private PatientMapper patientMapper;

    private ObjectMapper objectMapper;
    private PatientFacade facade;

    @Captor private ArgumentCaptor<OutboxEvent> outboxCaptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        facade = new PatientFacade(patientService, patientRepository, outboxRepository, objectMapper, billingGrpcClient, patientMapper);
    }

    @Nested
    @DisplayName("createPatient (REST)")
    class CreatePatientRest {

        @Test
        @DisplayName("success: writes outbox event in same transaction")
        void success_writesOutboxEvent() throws Exception {
            PatientRequestDTO dto = new PatientRequestDTO();
            dto.setEmail("new@patient.com");

            Patient patient = new Patient();
            patient.setPatientId("PAT-001");
            patient.setName("Test Patient");
            patient.setEmail("new@patient.com");

            PatientResponseDTO responseDTO = new PatientResponseDTO(
                null, "PAT-001", "Test Patient", "new@patient.com",
                null, null, null, null, null, null);

            when(patientRepository.existsByEmail("new@patient.com")).thenReturn(false);
            when(patientService.createPatient(dto)).thenReturn(patient);
            when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

            PatientResponseDTO result = facade.createPatient(dto);

            assertThat(result.patientId()).isEqualTo("PAT-001");

            verify(billingGrpcClient).createBillingAccount("PAT-001", "Test Patient", "new@patient.com");
            verify(outboxRepository).save(outboxCaptor.capture());

            OutboxEvent saved = outboxCaptor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("PATIENT");
            assertThat(saved.getAggregateId()).isEqualTo("PAT-001");
            assertThat(saved.getEventType()).isEqualTo("PATIENT_CREATED");
            assertThat(saved.getTopic()).isEqualTo("patient");
            assertThat(saved.getPartitionKey()).isEqualTo("PAT-001");
            assertThat(saved.isPublished()).isFalse();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getPublishedAt()).isNull();
            assertThat(saved.getPayload()).contains("\"patientId\":\"PAT-001\"");
            assertThat(saved.getPayload()).contains("\"eventType\":\"PATIENT_CREATED\"");
        }

        @Test
        @DisplayName("failure: duplicate email throws and no outbox event is written")
        void duplicateEmail_throwsAndNoOutbox() {
            PatientRequestDTO dto = new PatientRequestDTO();
            dto.setEmail("exists@test.com");

            when(patientRepository.existsByEmail("exists@test.com")).thenReturn(true);

            assertThatThrownBy(() -> facade.createPatient(dto))
                .isInstanceOf(EmailAlreadyExistsException.class);

            verify(outboxRepository, never()).save(any());
            verify(billingGrpcClient, never()).createBillingAccount(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("createPatient (gRPC)")
    class CreatePatientGrpc {

        @Test
        @DisplayName("success: writes outbox event")
        void success_writesOutboxEvent() throws Exception {
            com.pms.patientservice.dto.request.PatientGrpcRequestDTO dto =
                new com.pms.patientservice.dto.request.PatientGrpcRequestDTO(
                    "grpc-name", "grpc@email.com", "123", "addr", "1990-01-01", "M", "A+", "2026-07-13");

            Patient patient = new Patient();
            patient.setPatientId("PAT-002");
            patient.setName("grpc-name");
            patient.setEmail("grpc@email.com");

            PatientResponseDTO responseDTO = new PatientResponseDTO(
                null, "PAT-002", "grpc-name", "grpc@email.com",
                null, null, null, null, null, null);

            when(patientRepository.existsByEmail("grpc@email.com")).thenReturn(false);
            when(patientMapper.createPatient(dto)).thenReturn(patient);
            when(patientRepository.save(patient)).thenReturn(patient);
            when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

            PatientResponseDTO result = facade.createPatient(dto);

            assertThat(result.patientId()).isEqualTo("PAT-002");

            verify(billingGrpcClient).createBillingAccount("PAT-002", "grpc-name", "grpc@email.com");
            verify(outboxRepository).save(outboxCaptor.capture());

            OutboxEvent saved = outboxCaptor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("PATIENT");
            assertThat(saved.getAggregateId()).isEqualTo("PAT-002");
            assertThat(saved.getEventType()).isEqualTo("PATIENT_CREATED");
        }

        @Test
        @DisplayName("failure: duplicate email throws EmailAlreadyExistsException")
        void duplicateEmail_throws() {
            com.pms.patientservice.dto.request.PatientGrpcRequestDTO dto =
                new com.pms.patientservice.dto.request.PatientGrpcRequestDTO(
                    "dup", "dup@email.com", "123", "addr", "1990-01-01", "M", "A+", "2026-07-13");

            when(patientRepository.existsByEmail("dup@email.com")).thenReturn(true);

            assertThatThrownBy(() -> facade.createPatient(dto))
                .isInstanceOf(EmailAlreadyExistsException.class);

            verify(outboxRepository, never()).save(any());
            verify(billingGrpcClient, never()).createBillingAccount(any(), any(), any());
        }
    }
}
