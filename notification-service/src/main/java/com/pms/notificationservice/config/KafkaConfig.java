package com.pms.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.notificationservice.dto.event.AppointmentEventDTO;
import com.pms.notificationservice.dto.event.PrescriptionPdfGeneratedEventDTO;
import org.apache.kafka.common.serialization.StringDeserializer;
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

    @Bean
    public ConsumerFactory<String, AppointmentEventDTO> appointmentConsumerFactory(
            KafkaProperties properties, ObjectMapper objectMapper) {
        var config = properties.buildConsumerProperties(null);
        JsonDeserializer<AppointmentEventDTO> jsonDeserializer =
                new JsonDeserializer<>(AppointmentEventDTO.class, objectMapper);
        jsonDeserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AppointmentEventDTO> appointmentKafkaListenerContainerFactory(
            ConsumerFactory<String, AppointmentEventDTO> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, AppointmentEventDTO>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PrescriptionPdfGeneratedEventDTO> prescriptionConsumerFactory(
            KafkaProperties properties, ObjectMapper objectMapper) {
        var config = properties.buildConsumerProperties(null);
        JsonDeserializer<PrescriptionPdfGeneratedEventDTO> jsonDeserializer =
                new JsonDeserializer<>(PrescriptionPdfGeneratedEventDTO.class, objectMapper);
        jsonDeserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PrescriptionPdfGeneratedEventDTO> prescriptionKafkaListenerContainerFactory(
            ConsumerFactory<String, PrescriptionPdfGeneratedEventDTO> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, PrescriptionPdfGeneratedEventDTO>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    private CommonErrorHandler errorHandler() {
        return new DefaultErrorHandler((record, exception) -> {}, new FixedBackOff(0, 0));
    }

}
