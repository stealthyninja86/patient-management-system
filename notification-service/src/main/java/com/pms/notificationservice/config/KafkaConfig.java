package com.pms.notificationservice.config;

import com.pms.notificationservice.dto.event.AppointmentEventDTO;
import com.pms.notificationservice.dto.event.PrescriptionPdfGeneratedEventDTO;
import com.pms.notificationservice.dto.event.UserRegistrationEventDTO;
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

import java.util.HashMap;

@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public ConsumerFactory<String, AppointmentEventDTO> appointmentConsumerFactory(
            KafkaProperties properties) {
        var config = new HashMap<>(properties.buildConsumerProperties(null));
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AppointmentEventDTO.class.getName());
        JsonDeserializer<AppointmentEventDTO> jsonDeserializer = new JsonDeserializer<>();
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
            KafkaProperties properties) {
        var config = new HashMap<>(properties.buildConsumerProperties(null));
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PrescriptionPdfGeneratedEventDTO.class.getName());
        JsonDeserializer<PrescriptionPdfGeneratedEventDTO> jsonDeserializer = new JsonDeserializer<>();
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

    @Bean
    public ConsumerFactory<String, UserRegistrationEventDTO> userRegistrationConsumerFactory(
            KafkaProperties properties) {
        var config = new HashMap<>(properties.buildConsumerProperties(null));
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserRegistrationEventDTO.class.getName());
        JsonDeserializer<UserRegistrationEventDTO> jsonDeserializer = new JsonDeserializer<>();
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegistrationEventDTO> userRegistrationKafkaListenerContainerFactory(
            ConsumerFactory<String, UserRegistrationEventDTO> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, UserRegistrationEventDTO>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    private CommonErrorHandler errorHandler() {
        return new DefaultErrorHandler((record, exception) -> {
            log.error("Kafka consumer error after retries exhausted: topic={}, key={}, error={}",
                record.topic(), record.key(), exception.getMessage());
        }, new FixedBackOff(0, 0));
    }

}
