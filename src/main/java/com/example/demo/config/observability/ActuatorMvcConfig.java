package com.example.demo.config.observability;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.converter.HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ActuatorMvcConfig {

    private final WebMvcEndpointHandlerMapping webMvcEndpointHandlerMapping;

    @PostConstruct
    public void fixActuatorHandlerMappingOrder() {
        webMvcEndpointHandlerMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        log.info("✅ WebMvcEndpointHandlerMapping order → HIGHEST_PRECEDENCE");
    }

    /**
     * Registers a WebMvcConfigurer that adds the Prometheus text/plain
     * converter specifically for the actuator endpoint context.
     *
     * The EndpointObjectMapperWebMvcConfigurer registered by Spring Boot
     * actuator restricts converters for actuator endpoints. This configurer
     * ensures text/plain is always available for the prometheus scrape endpoint.
     */
    @Bean
    public WebMvcConfigurer prometheusMediaTypeConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void extendMessageConverters(
                    List<HttpMessageConverter<?>> converters) {

                // Check if prometheus converter already present
                boolean hasPrometheus = converters.stream()
                        .anyMatch(c -> c instanceof StringHttpMessageConverter sc &&
                                sc.getSupportedMediaTypes().stream()
                                        .anyMatch(m -> m.toString()
                                                .contains("openmetrics")));

                if (!hasPrometheus) {
                    StringHttpMessageConverter converter =
                            new StringHttpMessageConverter(StandardCharsets.UTF_8);
                    converter.setSupportedMediaTypes(Arrays.asList(
                            MediaType.parseMediaType(
                                    "application/openmetrics-text;version=1.0.0"),
                            MediaType.parseMediaType(
                                    "application/openmetrics-text;version=0.0.1"),
                            MediaType.parseMediaType(
                                    "text/plain;version=0.0.4"),
                            MediaType.TEXT_PLAIN,
                            MediaType.ALL
                    ));
                    // Add at position 0 so it is checked first
                    converters.add(0, converter);
                    log.info("✅ Prometheus StringHttpMessageConverter " +
                             "added to actuator configurer");
                }
            }
        };
    }
}