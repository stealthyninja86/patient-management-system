package com.pms.timelineservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Configuration
public class ObservabilityConfig {
  @Bean
  public Counter cacheHitCounter(MeterRegistry registry) {
    return Counter.builder("timeline.cache.hit")
            .description("Redis cache hits for timeline")
            .register(registry);
  }

  @Bean
  public Counter cacheMissCounter(MeterRegistry registry) {
    return Counter.builder("timeline.cache.miss")
            .description("Redis cache misses for timeline")
            .register(registry);
  }
}
