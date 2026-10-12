package com.example.demo.modules.observability.interfaces;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Direct Prometheus metrics endpoint.
 *
 * Does NOT use produces= on @GetMapping.
 * Instead writes directly to HttpServletResponse to bypass
 * Spring MVC content negotiation entirely.
 *
 * This avoids HttpMediaTypeNotAcceptableException caused by
 * WebConfig.configureContentNegotiation() not recognising
 * the Prometheus Accept header media types.
 */
@Slf4j
@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
public class PrometheusController {

    private final PrometheusMeterRegistry prometheusMeterRegistry;

    @GetMapping("/prometheus")
    public void prometheus(HttpServletResponse response) throws IOException {

        log.debug("Prometheus metrics scrape requested");

        // Scrape metrics
        String metrics = prometheusMeterRegistry.scrape();

        // Fix trailing comma bug in micrometer-registry-prometheus 1.12.0
        // Invalid:  {label="value",} 1.0
        // Valid:    {label="value"} 1.0
        String fixed = metrics.replace(",}", "}");

        // Write directly to response — bypasses Spring MVC content negotiation
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain;version=0.0.4;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(fixed.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);

        PrintWriter writer = response.getWriter();
        writer.write(fixed);
        writer.flush();

        log.debug("Prometheus scrape completed: {} bytes", fixed.length());
    }
}