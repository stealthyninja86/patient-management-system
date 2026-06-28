package com.pms.authservice.config;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ObservabilityConfig implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    SpanContext spanContext = Span.current().getSpanContext();
    if (spanContext.isValid()) {
      try {
        MDC.put("trace_id", spanContext.getTraceId());
        MDC.put("span_id", spanContext.getSpanId());
        MDC.put("trace_flags", spanContext.getTraceFlags().asHex());
        chain.doFilter(request, response);
      } finally {
        MDC.remove("trace_id");
        MDC.remove("span_id");
        MDC.remove("trace_flags");
      }
    } else {
      chain.doFilter(request, response);
    }
  }
}
