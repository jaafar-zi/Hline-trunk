package com.example.demo.config.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate configuration for synchronous HTTP calls
 *
 * Features:
 * - Connection timeout configuration
 * - Read timeout configuration
 * - Request/response logging
 * - Error handling
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * Default RestTemplate with basic configuration
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("Configuring default RestTemplate");

        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .requestFactory(() -> new BufferingClientHttpRequestFactory(clientHttpRequestFactory()))
                .interceptors((request, body, execution) -> {
                    log.debug("REST Request: {} {}", request.getMethod(), request.getURI());
                    var response = execution.execute(request, body);
                    log.debug("REST Response: {}", response.getStatusCode());
                    return response;
                })
                .build();
    }

    /**
     * RestTemplate for external API calls with longer timeout
     */
    @Bean("externalRestTemplate")
    public RestTemplate externalRestTemplate(RestTemplateBuilder builder) {
        log.info("Configuring external RestTemplate");

        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(60))
                .requestFactory(() -> new BufferingClientHttpRequestFactory(clientHttpRequestFactory()))
                .build();
    }

    /**
     * Client HTTP request factory
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return factory;
    }
}