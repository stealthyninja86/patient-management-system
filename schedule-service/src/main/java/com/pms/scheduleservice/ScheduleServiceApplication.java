package com.pms.scheduleservice;

import com.pms.scheduleservice.factory.AppointmentFactory;
import com.pms.scheduleservice.factory.TimeSlotFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ScheduleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleServiceApplication.class, args);
    }

    @Bean
    public AppointmentFactory appointmentFactory() {
        return new AppointmentFactory();
    }

    @Bean
    public TimeSlotFactory timeSlotFactory() {
        return new TimeSlotFactory();
    }
}
