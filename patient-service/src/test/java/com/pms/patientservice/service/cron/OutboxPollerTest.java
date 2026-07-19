package com.pms.patientservice.service.cron;

import com.pms.patientservice.model.OutboxEvent;
import com.pms.patientservice.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    @Mock private OutboxRepository outboxRepository;
    @Mock private KafkaTemplate<String, byte[]> kafkaTemplate;

    private OutboxPoller poller;

    @Captor private ArgumentCaptor<UUID> idCaptor;
    @Captor private ArgumentCaptor<LocalDateTime> timeCaptor;

    @BeforeEach
    void setUp() {
        poller = new OutboxPoller(outboxRepository, kafkaTemplate);
    }

    private OutboxEvent anEvent(UUID id, String topic, String key, String payload) {
        return new OutboxEvent(id, "PATIENT", key, "PATIENT_CREATED", topic, payload, key, false, LocalDateTime.now(), null);
    }

    @Nested
    @DisplayName("Success scenarios")
    class Success {

        @Test
        @DisplayName("publishes pending events and marks them as published")
        void publishesAndMarksPublished() {
            UUID eventId = UUID.randomUUID();
            List<OutboxEvent> events = List.of(anEvent(eventId, "patient", "PAT-001", "{\"eventType\":\"PATIENT_CREATED\"}"));

            when(outboxRepository.findUnpublishedEvents()).thenReturn(events);
            when(kafkaTemplate.send(eq("patient"), eq("PAT-001"), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

            poller.publishPendingEvents();

            verify(outboxRepository).markPublished(eq(eventId), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("skips cleanly when no events are pending")
        void noEvents_doesNothing() {
            when(outboxRepository.findUnpublishedEvents()).thenReturn(List.of());

            poller.publishPendingEvents();

            verify(kafkaTemplate, never()).send(anyString(), any());
            verify(outboxRepository, never()).markPublished(any(), any());
        }

        @Test
        @DisplayName("sends without partition key when null")
        void sendsWithoutPartitionKey() {
            UUID eventId = UUID.randomUUID();
            OutboxEvent event = new OutboxEvent(eventId, "PATIENT", "PAT-001", "EVENT", "topic", "{}", null,
                false, LocalDateTime.now(), null);

            when(outboxRepository.findUnpublishedEvents()).thenReturn(List.of(event));
            when(kafkaTemplate.send(eq("topic"), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

            poller.publishPendingEvents();

            verify(kafkaTemplate).send(eq("topic"), any(byte[].class));
            verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("Failure scenarios")
    class Failure {

        @Test
        @DisplayName("when Kafka send fails, event remains unpublished and poller continues")
        void kafkaFailure_skipsAndContinues() {
            UUID goodId = UUID.randomUUID();
            UUID badId = UUID.randomUUID();

            OutboxEvent goodEvent = anEvent(goodId, "topic-good", "K1", "{}");
            OutboxEvent badEvent = anEvent(badId, "topic-bad", "K2", "{}");

            when(outboxRepository.findUnpublishedEvents()).thenReturn(List.of(goodEvent, badEvent));
            when(kafkaTemplate.send(eq("topic-good"), eq("K1"), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
            when(kafkaTemplate.send(eq("topic-bad"), eq("K2"), any(byte[].class)))
                .thenThrow(new RuntimeException("Kafka broker unreachable"));

            poller.publishPendingEvents();

            verify(outboxRepository).markPublished(eq(goodId), any(LocalDateTime.class));
            verify(outboxRepository, never()).markPublished(eq(badId), any());
        }

        @Test
        @DisplayName("when future times out, event stays unpublished")
        void kafkaTimeout_eventStaysUnpublished() {
            UUID eventId = UUID.randomUUID();
            OutboxEvent event = anEvent(eventId, "patient", "PAT-001", "{}");

            CompletableFuture<SendResult<String, byte[]>> slowFuture = new CompletableFuture<>();

            when(outboxRepository.findUnpublishedEvents()).thenReturn(List.of(event));
            when(kafkaTemplate.send(eq("patient"), eq("PAT-001"), any(byte[].class)))
                .thenReturn(slowFuture);

            poller.publishPendingEvents();

            verify(outboxRepository, never()).markPublished(any(), any());
            slowFuture.complete(mock(SendResult.class));
        }
    }
}
