package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActuatorDiagnostic {

    private final ApplicationContext context;

    @PostConstruct
    public void diagnose() {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║         ACTUATOR / PROMETHEUS DIAGNOSTIC        ║");
        log.info("╚══════════════════════════════════════════════════╝");

        // 1. Check PrometheusMeterRegistry
        checkBean("prometheusMeterRegistry",
                  "io.micrometer.prometheus.PrometheusMeterRegistry");
        checkBean("prometheusMeterRegistry",
                  "io.micrometer.prometheusmetrics.PrometheusMeterRegistry");

        // 2. Check CollectorRegistry
        checkBean("collectorRegistry",
                  "io.prometheus.client.CollectorRegistry");
        checkBeanByType("io.prometheus.client.CollectorRegistry");

        // 3. Check PrometheusScrapeEndpoint
        checkBean("prometheusEndpoint", null);
        checkBean("prometheusScrapeEndpoint", null);
        checkBeanByType(
            "org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint");

        // 4. All beans containing "prometheus"
        log.info("── Beans containing 'prometheus' ──");
        int count = 0;
        for (String name : context.getBeanDefinitionNames()) {
            if (name.toLowerCase().contains("prometheus")) {
                log.info("  ✅ bean: [{}] → {}",
                    name,
                    context.getBean(name).getClass().getName());
                count++;
            }
        }
        if (count == 0) {
            log.error("  ❌ NO beans found containing 'prometheus'!");
        }

        // 5. All beans containing "meter" or "metric"
        log.info("── Beans containing 'meter' or 'metric' ──");
        for (String name : context.getBeanDefinitionNames()) {
            if (name.toLowerCase().contains("meter") ||
                name.toLowerCase().contains("metric")) {
                log.info("  bean: [{}] → {}",
                    name,
                    context.getBean(name).getClass().getName());
            }
        }

        // 6. All beans containing "endpoint"
        log.info("── Beans containing 'endpoint' ──");
        for (String name : context.getBeanDefinitionNames()) {
            if (name.toLowerCase().contains("endpoint") &&
                !name.toLowerCase().contains("kafka")) {
                log.info("  bean: [{}] → {}",
                    name,
                    context.getBean(name).getClass().getName());
            }
        }

        // 7. Handler mappings
        log.info("── Handler Mappings ──");
        Map<String, HandlerMapping> mappings =
            context.getBeansOfType(HandlerMapping.class);
        mappings.forEach((name, mapping) ->
            log.info("  [{}] → {}",
                name,
                mapping.getClass().getName())
        );

        boolean hasActuator = mappings.values().stream()
            .anyMatch(m -> m.getClass().getName()
                .contains("WebMvcEndpointHandlerMapping"));
        if (hasActuator) {
            log.info("  ✅ WebMvcEndpointHandlerMapping IS registered");
        } else {
            log.error("  ❌ WebMvcEndpointHandlerMapping NOT registered!");
        }

        // 8. Auto-configuration report
        log.info("── Auto-Configuration Check ──");
        checkBean(
            "prometheusMetricsExportAutoConfiguration", null);
        checkBean(
            "org.springframework.boot.actuate.autoconfigure.metrics" +
            ".export.prometheus.PrometheusMetricsExportAutoConfiguration",
            null);

        log.info("══════════════════════════════════════════════════");
    }

    private void checkBean(String name, String expectedType) {
        try {
            Object bean = context.getBean(name);
            log.info("  ✅ Bean '{}' → {}",
                name, bean.getClass().getName());
        } catch (Exception e) {
            log.warn("  ❌ Bean '{}' NOT found (expected: {})",
                name,
                expectedType != null ? expectedType : "any");
        }
    }

    private void checkBeanByType(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Map<String, ?> beans = context.getBeansOfType(clazz);
            if (beans.isEmpty()) {
                log.warn("  ❌ No beans of type '{}' found", className);
            } else {
                beans.forEach((name, bean) ->
                    log.info("  ✅ Bean of type '{}': [{}]",
                        className, name));
            }
        } catch (ClassNotFoundException e) {
            log.error("  ❌ Class '{}' NOT on classpath!", className);
        }
    }
}