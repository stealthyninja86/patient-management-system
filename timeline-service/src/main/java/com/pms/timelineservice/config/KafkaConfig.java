package com.pms.timelineservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.timelineservice.dto.event.AppointmentEvent;
import com.pms.timelineservice.dto.event.ConsentGrantedEvent;
import com.pms.timelineservice.dto.event.PrescriptionPdfGeneratedEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    // ── Prescription events (JSON) ──

    @Bean
    public ConsumerFactory<String, PrescriptionPdfGeneratedEvent> prescriptionConsumerFactory(
            KafkaProperties properties, ObjectMapper objectMapper) {
        var config = properties.buildConsumerProperties(null);
        var jsonDeser = new JsonDeserializer<>(PrescriptionPdfGeneratedEvent.class, objectMapper);
        jsonDeser.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeser));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PrescriptionPdfGeneratedEvent>
            prescriptionKafkaListenerContainerFactory(
                    ConsumerFactory<String, PrescriptionPdfGeneratedEvent> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, PrescriptionPdfGeneratedEvent>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    // ── Appointment events (JSON) ──

    @Bean
    public ConsumerFactory<String, AppointmentEvent> appointmentConsumerFactory(
            KafkaProperties properties, ObjectMapper objectMapper) {
        var config = properties.buildConsumerProperties(null);
        var jsonDeser = new JsonDeserializer<>(AppointmentEvent.class, objectMapper);
        jsonDeser.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeser));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AppointmentEvent>
            appointmentKafkaListenerContainerFactory(
                    ConsumerFactory<String, AppointmentEvent> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, AppointmentEvent>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    // ── Consent events (JSON) ──

    @Bean
    public ConsumerFactory<String, ConsentGrantedEvent> consentConsumerFactory(
            KafkaProperties properties, ObjectMapper objectMapper) {
        var config = properties.buildConsumerProperties(null);
        var jsonDeser = new JsonDeserializer<>(ConsentGrantedEvent.class, objectMapper);
        jsonDeser.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeser));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ConsentGrantedEvent>
            consentKafkaListenerContainerFactory(
                    ConsumerFactory<String, ConsentGrantedEvent> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ConsentGrantedEvent>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    // ── Shared error handler ──

    private CommonErrorHandler errorHandler() {
        return new DefaultErrorHandler((record, exception) ->
            log.error("Kafka error after retries exhausted: topic={}, key={}, error={}",
                record.topic(), record.key(), exception.getMessage()),
            new FixedBackOff(0, 0));
    }
}
